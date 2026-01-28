/*
 * ESP32 HVAC Telemetry Simulator
 *
 * This code simulates a YORK Ducted HVAC unit sending telemetry data
 * to the IoT HVAC Monitoring System via MQTT.
 *
 * Hardware: ESP32 DevKit
 *
 * MQTT Topics:
 *   - hvac/{device_id}/telemetry  : Publishes sensor data
 *   - hvac/{device_id}/status     : Publishes heartbeat
 *   - hvac/{device_id}/control    : Subscribes to control commands
 */

#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

// ==================== CONFIGURATION ====================

// WiFi Configuration
const char* WIFI_SSID = "YOUR_WIFI_SSID";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";

// MQTT Configuration
const char* MQTT_SERVER = "trolley.proxy.rlwy.net";
const int MQTT_PORT = 26703;
const char* MQTT_USERNAME = "generator-monitoring-system";
const char* MQTT_PASSWORD = "di1u5ydet0z049vbbl08cofp6vhya45l";

// Device Configuration
const char* DEVICE_ID = "YORK-001";  // Change this to match your device ID

// Timing Configuration
const unsigned long TELEMETRY_INTERVAL = 10000;  // Send telemetry every 10 seconds
const unsigned long HEARTBEAT_INTERVAL = 30000;  // Send heartbeat every 30 seconds

// ==================== GLOBAL VARIABLES ====================

WiFiClient espClient;
PubSubClient mqttClient(espClient);

unsigned long lastTelemetryTime = 0;
unsigned long lastHeartbeatTime = 0;

// Simulated device state
bool powerStatus = true;
String mode = "COOLING";
String fanSpeed = "MED";
float temperatureSetpoint = 22.0;
bool compressorStatus = true;

// Topics
String telemetryTopic;
String statusTopic;
String controlTopic;

// ==================== SETUP ====================

void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.println("\n\n========================================");
  Serial.println("   ESP32 HVAC Telemetry Simulator");
  Serial.println("========================================\n");

  // Initialize topics
  telemetryTopic = String("hvac/") + DEVICE_ID + "/telemetry";
  statusTopic = String("hvac/") + DEVICE_ID + "/status";
  controlTopic = String("hvac/") + DEVICE_ID + "/control";

  // Connect to WiFi
  setupWiFi();

  // Configure MQTT
  mqttClient.setServer(MQTT_SERVER, MQTT_PORT);
  mqttClient.setCallback(mqttCallback);
  mqttClient.setBufferSize(1024);

  // Initial connection
  connectMQTT();

  Serial.println("\nSetup complete. Starting telemetry...\n");
}

// ==================== MAIN LOOP ====================

void loop() {
  // Ensure MQTT connection
  if (!mqttClient.connected()) {
    connectMQTT();
  }
  mqttClient.loop();

  unsigned long currentTime = millis();

  // Send telemetry at interval
  if (currentTime - lastTelemetryTime >= TELEMETRY_INTERVAL) {
    sendTelemetry();
    lastTelemetryTime = currentTime;
  }

  // Send heartbeat at interval
  if (currentTime - lastHeartbeatTime >= HEARTBEAT_INTERVAL) {
    sendHeartbeat();
    lastHeartbeatTime = currentTime;
  }

  delay(100);
}

// ==================== WIFI ====================

void setupWiFi() {
  Serial.print("Connecting to WiFi: ");
  Serial.println(WIFI_SSID);

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 30) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi connected!");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\nWiFi connection failed! Restarting...");
    ESP.restart();
  }
}

// ==================== MQTT ====================

void connectMQTT() {
  while (!mqttClient.connected()) {
    Serial.print("Connecting to MQTT broker...");

    String clientId = "ESP32_" + String(DEVICE_ID) + "_" + String(random(0xffff), HEX);

    if (mqttClient.connect(clientId.c_str(), MQTT_USERNAME, MQTT_PASSWORD)) {
      Serial.println(" connected!");

      // Subscribe to control topic
      mqttClient.subscribe(controlTopic.c_str());
      Serial.print("Subscribed to: ");
      Serial.println(controlTopic);

      // Send initial heartbeat
      sendHeartbeat();

    } else {
      Serial.print(" failed, rc=");
      Serial.print(mqttClient.state());
      Serial.println(". Retrying in 5 seconds...");
      delay(5000);
    }
  }
}

void mqttCallback(char* topic, byte* payload, unsigned int length) {
  Serial.print("\nReceived message on topic: ");
  Serial.println(topic);

  // Parse JSON payload
  StaticJsonDocument<256> doc;
  DeserializationError error = deserializeJson(doc, payload, length);

  if (error) {
    Serial.print("JSON parse error: ");
    Serial.println(error.c_str());
    return;
  }

  // Handle control commands
  if (String(topic) == controlTopic) {
    handleControlCommand(doc);
  }
}

void handleControlCommand(JsonDocument& doc) {
  if (doc.containsKey("type")) {
    String type = doc["type"].as<String>();

    if (type == "POWER") {
      String status = doc["value"].as<String>();
      powerStatus = (status == "ON");
      Serial.print("Power set to: ");
      Serial.println(status);
    }
    else if (type == "MODE") {
      mode = doc["value"].as<String>();
      Serial.print("Mode set to: ");
      Serial.println(mode);
    }
    else if (type == "FAN_SPEED") {
      fanSpeed = doc["value"].as<String>();
      Serial.print("Fan speed set to: ");
      Serial.println(fanSpeed);
    }
    else if (type == "TEMPERATURE") {
      temperatureSetpoint = doc["value"].as<float>();
      Serial.print("Temperature setpoint: ");
      Serial.println(temperatureSetpoint);
    }
  }
}

// ==================== TELEMETRY ====================

void sendTelemetry() {
  if (!powerStatus) {
    Serial.println("[Telemetry] Device is OFF, sending minimal data");
  }

  // Generate simulated sensor data
  float supplyTemp = generateSupplyTemp();
  float returnTemp = supplyTemp + random(80, 120) / 10.0;
  float roomTemp = generateRoomTemp();
  float humidity = random(400, 700) / 10.0;
  float outdoorTemp = random(250, 400) / 10.0;
  float voltage = random(2200, 2400) / 10.0;
  float current = powerStatus ? random(80, 160) / 10.0 : 0.1;
  float power = voltage * current / 1000.0;
  float filterCondition = random(100, 450) / 10.0;
  float refrigerantPressure = random(800, 1200) / 10.0;

  // Create JSON payload
  StaticJsonDocument<512> doc;
  doc["deviceId"] = DEVICE_ID;
  doc["supplyTemp"] = round(supplyTemp * 10) / 10.0;
  doc["returnTemp"] = round(returnTemp * 10) / 10.0;
  doc["roomTemp"] = round(roomTemp * 10) / 10.0;
  doc["humidity"] = round(humidity * 10) / 10.0;
  doc["outdoorTemp"] = round(outdoorTemp * 10) / 10.0;
  doc["voltage"] = round(voltage * 10) / 10.0;
  doc["current"] = round(current * 10) / 10.0;
  doc["power"] = round(power * 100) / 100.0;
  doc["compressorStatus"] = powerStatus && compressorStatus;
  doc["fanSpeed"] = fanSpeed;
  doc["airflowStatus"] = powerStatus;
  doc["filterCondition"] = round(filterCondition * 10) / 10.0;
  doc["refrigerantPressure"] = round(refrigerantPressure * 10) / 10.0;

  char buffer[512];
  serializeJson(doc, buffer);

  if (mqttClient.publish(telemetryTopic.c_str(), buffer)) {
    Serial.print("[Telemetry] Sent: Room=");
    Serial.print(roomTemp, 1);
    Serial.print("°C, Supply=");
    Serial.print(supplyTemp, 1);
    Serial.print("°C, Power=");
    Serial.print(power, 2);
    Serial.println("kW");
  } else {
    Serial.println("[Telemetry] Failed to send!");
  }
}

void sendHeartbeat() {
  StaticJsonDocument<128> doc;
  doc["deviceId"] = DEVICE_ID;
  doc["status"] = powerStatus ? "ON" : "OFF";
  doc["mode"] = mode;
  doc["fanSpeed"] = fanSpeed;
  doc["setpoint"] = temperatureSetpoint;
  doc["uptime"] = millis() / 1000;

  char buffer[128];
  serializeJson(doc, buffer);

  if (mqttClient.publish(statusTopic.c_str(), buffer)) {
    Serial.println("[Heartbeat] Sent");
  } else {
    Serial.println("[Heartbeat] Failed!");
  }
}

// ==================== SIMULATION ====================

float generateSupplyTemp() {
  if (!powerStatus) {
    return random(200, 260) / 10.0;  // Room temperature when off
  }

  if (mode == "COOLING") {
    return random(120, 180) / 10.0;  // 12-18°C for cooling
  } else {
    return random(350, 450) / 10.0;  // 35-45°C for heating
  }
}

float generateRoomTemp() {
  // Simulate room temperature approaching setpoint
  static float currentRoomTemp = 25.0;

  if (powerStatus) {
    float diff = temperatureSetpoint - currentRoomTemp;
    currentRoomTemp += diff * 0.05 + (random(-10, 10) / 100.0);
  } else {
    // Slowly drift toward outdoor temperature
    currentRoomTemp += (random(-5, 5) / 100.0);
  }

  // Keep within reasonable bounds
  currentRoomTemp = constrain(currentRoomTemp, 15.0, 35.0);

  return currentRoomTemp;
}
