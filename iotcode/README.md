# ESP32 HVAC Telemetry Simulator

This ESP32 code simulates a YORK Ducted HVAC unit for testing the IoT HVAC Monitoring System.

## Features

- Publishes simulated telemetry data via MQTT every 10 seconds
- Sends heartbeat/status updates every 30 seconds
- Subscribes to control commands (power, mode, fan speed, temperature)
- Generates realistic sensor values based on device state

## Hardware Requirements

- ESP32 DevKit (any variant)
- USB cable for programming

## Software Requirements

- Arduino IDE 2.0+ or PlatformIO
- ESP32 Board Package
- Required Libraries:
  - `PubSubClient` (MQTT client)
  - `ArduinoJson` (JSON parsing)

## Installation

### Arduino IDE

1. Install ESP32 board package:
   - Go to File → Preferences
   - Add to Board Manager URLs: `https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json`
   - Go to Tools → Board → Boards Manager
   - Search "esp32" and install

2. Install required libraries:
   - Go to Sketch → Include Library → Manage Libraries
   - Install `PubSubClient` by Nick O'Leary
   - Install `ArduinoJson` by Benoit Blanchon

3. Configure the code:
   - Open `esp32_hvac_simulator.ino`
   - Update WiFi credentials:
     ```cpp
     const char* WIFI_SSID = "YOUR_WIFI_SSID";
     const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
     ```
   - Update Device ID if needed:
     ```cpp
     const char* DEVICE_ID = "YORK-001";
     ```

4. Upload to ESP32:
   - Select your ESP32 board from Tools → Board
   - Select the correct COM port
   - Click Upload

## MQTT Topics

| Topic | Direction | Description |
|-------|-----------|-------------|
| `hvac/{device_id}/telemetry` | Publish | Sensor data (every 10s) |
| `hvac/{device_id}/status` | Publish | Heartbeat/status (every 30s) |
| `hvac/{device_id}/control` | Subscribe | Control commands |

## Telemetry Payload

```json
{
  "deviceId": "YORK-001",
  "supplyTemp": 15.2,
  "returnTemp": 23.5,
  "roomTemp": 22.1,
  "humidity": 55.3,
  "outdoorTemp": 32.5,
  "voltage": 228.5,
  "current": 12.3,
  "power": 2.81,
  "compressorStatus": true,
  "fanSpeed": "MED",
  "airflowStatus": true,
  "filterCondition": 25.5,
  "refrigerantPressure": 95.2
}
```

## Control Commands

The device responds to these control commands:

### Power Control
```json
{"type": "POWER", "value": "ON"}
{"type": "POWER", "value": "OFF"}
```

### Mode Control
```json
{"type": "MODE", "value": "COOLING"}
{"type": "MODE", "value": "HEATING"}
```

### Fan Speed Control
```json
{"type": "FAN_SPEED", "value": "LOW"}
{"type": "FAN_SPEED", "value": "MED"}
{"type": "FAN_SPEED", "value": "HIGH"}
```

### Temperature Setpoint
```json
{"type": "TEMPERATURE", "value": 22.5}
```

## Testing Without ESP32

You can test MQTT connectivity using `mosquitto_pub`:

```bash
# Send telemetry
mosquitto_pub -h trolley.proxy.rlwy.net -p 26703 \
  -u generator-monitoring-system \
  -P di1u5ydet0z049vbbl08cofp6vhya45l \
  -t "hvac/YORK-001/telemetry" \
  -m '{"deviceId":"YORK-001","supplyTemp":15.2,"returnTemp":23.5,"roomTemp":22.1,"humidity":55.3,"outdoorTemp":32.5,"voltage":228.5,"current":12.3,"power":2.81,"compressorStatus":true,"fanSpeed":"MED","airflowStatus":true,"filterCondition":25.5,"refrigerantPressure":95.2}'

# Send heartbeat
mosquitto_pub -h trolley.proxy.rlwy.net -p 26703 \
  -u generator-monitoring-system \
  -P di1u5ydet0z049vbbl08cofp6vhya45l \
  -t "hvac/YORK-001/status" \
  -m '{"deviceId":"YORK-001","status":"ON","mode":"COOLING","fanSpeed":"MED","setpoint":22.0}'
```

## Serial Monitor Output

```
========================================
   ESP32 HVAC Telemetry Simulator
========================================

Connecting to WiFi: MyNetwork
......
WiFi connected!
IP Address: 192.168.1.105
Connecting to MQTT broker... connected!
Subscribed to: hvac/YORK-001/control
[Heartbeat] Sent

[Telemetry] Sent: Room=22.3°C, Supply=15.1°C, Power=2.75kW
[Telemetry] Sent: Room=22.1°C, Supply=14.8°C, Power=2.82kW
[Heartbeat] Sent
```

## Troubleshooting

1. **WiFi not connecting**: Check SSID and password
2. **MQTT connection failed**: Verify broker URL, port, and credentials
3. **No data in dashboard**: Ensure Device ID matches one in the database
4. **Compilation errors**: Ensure all libraries are installed

## Multiple Devices

To simulate multiple devices:
1. Copy the code to a new file
2. Change `DEVICE_ID` to match another device (e.g., "YORK-002")
3. Upload to another ESP32

## License

MIT License
