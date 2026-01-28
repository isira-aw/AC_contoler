package com.hvac.iot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hvac.iot.dto.TelemetryDTO;
import com.hvac.iot.model.Device;
import com.hvac.iot.repository.DeviceRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttService implements MqttCallback {

    private final MqttClient mqttClient;
    private final ObjectMapper objectMapper;
    private final DeviceRepository deviceRepository;
    private final TelemetryService telemetryService;

    private static final String TELEMETRY_TOPIC_PATTERN = "hvac/+/telemetry";
    private static final String STATUS_TOPIC_PATTERN = "hvac/+/status";
    private static final String CONTROL_TOPIC_PREFIX = "hvac/";

    @PostConstruct
    public void init() {
        try {
            if (!mqttClient.isConnected()) {
                MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(true);
                options.setCleanSession(true);
                mqttClient.connect(options);
            }

            mqttClient.setCallback(this);

            // Subscribe to telemetry and status topics for all devices
            mqttClient.subscribe(TELEMETRY_TOPIC_PATTERN, 1);
            mqttClient.subscribe(STATUS_TOPIC_PATTERN, 1);

            log.info("Subscribed to MQTT topics: {}, {}", TELEMETRY_TOPIC_PATTERN, STATUS_TOPIC_PATTERN);
        } catch (MqttException e) {
            log.error("Failed to initialize MQTT subscriptions", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
                log.info("Disconnected from MQTT broker");
            }
        } catch (MqttException e) {
            log.error("Error disconnecting from MQTT broker", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost: {}", cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            log.debug("MQTT message received - Topic: {}, Payload: {}", topic, payload);

            // Parse device ID from topic: hvac/{device_id}/telemetry or hvac/{device_id}/status
            String[] topicParts = topic.split("/");
            if (topicParts.length < 3) {
                log.warn("Invalid topic format: {}", topic);
                return;
            }

            String deviceId = topicParts[1];
            String messageType = topicParts[2];

            // Validate device exists
            if (!deviceRepository.existsById(deviceId)) {
                log.warn("Received message for unknown device: {}", deviceId);
                return;
            }

            if ("telemetry".equals(messageType)) {
                processTelemetryMessage(deviceId, payload);
            } else if ("status".equals(messageType)) {
                processStatusMessage(deviceId, payload);
            }

        } catch (Exception e) {
            log.error("Error processing MQTT message", e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("MQTT message delivery complete");
    }

    private void processTelemetryMessage(String deviceId, String payload) {
        try {
            JsonNode json = objectMapper.readTree(payload);

            TelemetryDTO dto = TelemetryDTO.builder()
                    .deviceId(deviceId)
                    .timestamp(LocalDateTime.now())
                    .supplyTemp(getFloatValue(json, "supply_temp"))
                    .returnTemp(getFloatValue(json, "return_temp"))
                    .roomTemp(getFloatValue(json, "room_temp"))
                    .humidity(getFloatValue(json, "humidity"))
                    .outdoorTemp(getFloatValue(json, "outdoor_temp"))
                    .voltage(getFloatValue(json, "voltage"))
                    .current(getFloatValue(json, "current"))
                    .energy(getFloatValue(json, "energy"))
                    .compressorStatus(getBooleanValue(json, "compressor_status"))
                    .fanSpeed(getStringValue(json, "fan_speed"))
                    .airflowStatus(getBooleanValue(json, "airflow_status"))
                    .filterCondition(getFloatValue(json, "filter_condition"))
                    .refrigerantPressure(getFloatValue(json, "refrigerant_pressure"))
                    .build();

            telemetryService.saveTelemetry(deviceId, dto);
            log.debug("Saved telemetry for device: {}", deviceId);

        } catch (Exception e) {
            log.error("Error processing telemetry message for device: {}", deviceId, e);
        }
    }

    private void processStatusMessage(String deviceId, String payload) {
        try {
            // Update device heartbeat
            Device device = deviceRepository.findById(deviceId).orElse(null);
            if (device != null) {
                device.setLastHeartbeat(LocalDateTime.now());
                deviceRepository.save(device);
                log.debug("Updated heartbeat for device: {}", deviceId);
            }
        } catch (Exception e) {
            log.error("Error processing status message for device: {}", deviceId, e);
        }
    }

    public void publishControlCommand(String deviceId, String commandType, String value) {
        try {
            if (!mqttClient.isConnected()) {
                log.warn("MQTT client not connected. Cannot publish command.");
                return;
            }

            String topic = CONTROL_TOPIC_PREFIX + deviceId + "/control";
            String payload = objectMapper.writeValueAsString(new ControlPayload(commandType, value));

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            message.setRetained(false);

            mqttClient.publish(topic, message);
            log.info("Published control command - Device: {}, Type: {}, Value: {}", deviceId, commandType, value);

        } catch (Exception e) {
            log.error("Failed to publish control command", e);
        }
    }

    private Float getFloatValue(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node != null && !node.isNull() ? node.floatValue() : null;
    }

    private Boolean getBooleanValue(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node != null && !node.isNull() ? node.booleanValue() : null;
    }

    private String getStringValue(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node != null && !node.isNull() ? node.asText() : null;
    }

    // Inner class for control payload
    private record ControlPayload(String command, String value) {}
}
