# IoT-Enabled Smart Monitoring & Control System for YORK Ducted HVAC Units

A complete, production-ready IoT monitoring and control system for HVAC units with real-time telemetry, fault detection, predictive analytics, and multi-role access control.

## Features

- **Real-time Monitoring**: Live telemetry data including temperature, humidity, power consumption, and more
- **Device Control**: Remote ON/OFF, mode selection, fan speed, and temperature setpoint control
- **Fault Detection**: Automatic detection of overcurrent, phase failure, overheating, filter choke, and sensor failures
- **Predictive Analytics**: Runtime estimation, energy predictions, and efficiency scoring
- **Multi-Role Access Control**: SuperAdmin, DeviceOwner, and DeviceUser roles with granular permissions
- **Team-Based Management**: Organize devices and users into teams
- **PDF Export**: Export fault logs as PDF reports
- **MQTT Integration**: Real-time communication with HVAC devices

## Tech Stack

### Backend
- Spring Boot 3.2
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL / H2 (development)
- Eclipse Paho MQTT Client
- iText PDF Generation
- Maven

### Frontend
- React 18 with Vite
- React Router v6
- Axios
- Recharts
- TailwindCSS

## Project Structure

```
project-root/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/com/hvac/iot/
│   │   ├── config/            # Configuration classes
│   │   ├── controller/        # REST Controllers
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── model/             # JPA Entities
│   │   ├── repository/        # JPA Repositories
│   │   ├── security/          # JWT and Security
│   │   └── service/           # Business Logic
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
└── frontend/                   # React Frontend
    ├── src/
    │   ├── components/        # Reusable Components
    │   ├── pages/             # Page Components
    │   │   ├── superadmin/
    │   │   ├── owner/
    │   │   └── user/
    │   ├── services/          # API Services
    │   └── utils/             # Utility Functions
    ├── package.json
    └── vite.config.js
```

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- PostgreSQL 14+ (optional, H2 used by default)
- Maven 3.8+

## Quick Start

### Backend Setup

1. Navigate to the backend directory:
```bash
cd backend
```

2. Configure the database (optional - H2 is used by default):
```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hvac_iot
spring.datasource.username=postgres
spring.datasource.password=yourpassword
```

3. Build and run the backend:
```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The frontend will start on `http://localhost:5173`

## Default Login Credentials

After starting the application, seed data is automatically created:

| Role | Email | Password |
|------|-------|----------|
| SuperAdmin | admin@hvac.com | admin123 |
| DeviceOwner | john@example.com | owner123 |
| DeviceOwner | sarah@example.com | owner123 |
| DeviceUser | mike@example.com | user123 |
| DeviceUser | emily@example.com | user123 |
| DeviceUser | david@example.com | user123 |

## User Roles & Permissions

### SuperAdmin
- Register and manage DeviceOwners
- Register and manage Devices
- Control device licenses
- View all devices, users, and telemetry
- Cannot control devices directly

### DeviceOwner
- Create and manage teams
- Register and manage DeviceUsers
- Assign users to teams
- Control and monitor owned devices
- Transfer device ownership
- Export fault logs as PDF
- View predictions

### DeviceUser
- Control assigned devices (ON/OFF, temperature, fan speed)
- Monitor real-time telemetry
- View fault logs
- View predictions

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token
- `POST /api/auth/reset-password-request` - Request password reset
- `POST /api/auth/reset-password` - Reset password

### SuperAdmin
- `GET/POST /api/superadmin/device-owners` - Manage device owners
- `GET/POST/PUT/DELETE /api/superadmin/devices` - Manage devices

### DeviceOwner
- `GET/POST/DELETE /api/owner/teams` - Manage teams
- `GET/POST/PUT/DELETE /api/owner/users` - Manage users
- `GET /api/owner/devices` - List owned devices
- `PUT /api/owner/devices/{id}/transfer` - Transfer ownership
- `GET /api/owner/devices/{id}/faults/export-pdf` - Export faults

### Device Control
- `POST /api/control/{deviceId}/on-off` - Power control
- `POST /api/control/{deviceId}/mode` - Mode control
- `POST /api/control/{deviceId}/fan-speed` - Fan speed control
- `POST /api/control/{deviceId}/temperature` - Temperature setpoint

### Telemetry & Predictions
- `GET /api/owner/devices/{id}/telemetry` - Get telemetry data
- `GET /api/owner/devices/{id}/faults` - Get fault logs
- `GET /api/predictions/{deviceId}` - Get predictions

## MQTT Topics

```
hvac/{device_id}/telemetry   # Device publishes sensor data
hvac/{device_id}/control     # Backend publishes control commands
hvac/{device_id}/status      # Device publishes heartbeat
```

## Fault Detection Rules

| Fault Type | Condition |
|------------|-----------|
| Overcurrent | current > 20A |
| Phase Failure | voltage < 200V or > 250V |
| Overheating | supply_temp > 45°C |
| Filter Choke | filter_condition > 50 Pa |
| Sensor Failure | No data or out-of-range values |
| Device Offline | No heartbeat for > 2 minutes |

## Data Retention

- **Telemetry**: Auto-deleted after 7 days
- **Fault Logs**: Kept indefinitely

## Environment Variables

### Backend (application.properties)
```properties
# Database
spring.datasource.url=jdbc:h2:mem:hvac_iot
spring.datasource.username=sa
spring.datasource.password=

# MQTT
mqtt.broker.url=tcp://broker.hivemq.com:1883
mqtt.client.id=hvac-backend
mqtt.username=
mqtt.password=

# JWT
jwt.secret=your-256-bit-secret-key-here
jwt.expiration=86400000

# Email (optional)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### Frontend (.env)
```
VITE_API_BASE_URL=http://localhost:8080/api
```

## Production Deployment

### Backend
```bash
cd backend
mvn clean package -DskipTests
java -jar target/iot-hvac-monitoring-1.0.0.jar
```

### Frontend
```bash
cd frontend
npm run build
# Serve the dist folder with nginx or similar
```

## License

MIT License - See LICENSE file for details

## Support

For issues and feature requests, please create an issue in the repository.
