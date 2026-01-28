package com.hvac.iot.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@Slf4j
public class MqttConfig {

    @Value("${mqtt.broker.url:tcp://broker.hivemq.com:1883}")
    private String brokerUrl;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.client.id:hvac-iot-backend}")
    private String clientIdPrefix;

    @Bean
    public MqttClient mqttClient() {
        try {
            String clientId = clientIdPrefix + "-" + UUID.randomUUID().toString().substring(0, 8);
            MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(60);

            if (username != null && !username.isEmpty()) {
                options.setUserName(username);
            }
            if (password != null && !password.isEmpty()) {
                options.setPassword(password.toCharArray());
            }

            client.connect(options);
            log.info("Connected to MQTT broker: {}", brokerUrl);

            return client;
        } catch (MqttException e) {
            log.error("Failed to connect to MQTT broker: {}", brokerUrl, e);
            // Return a disconnected client - service will handle reconnection
            try {
                String clientId = clientIdPrefix + "-" + UUID.randomUUID().toString().substring(0, 8);
                return new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            } catch (MqttException ex) {
                throw new RuntimeException("Failed to create MQTT client", ex);
            }
        }
    }
}
