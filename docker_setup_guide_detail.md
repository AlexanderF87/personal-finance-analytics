# 🐳 Docker Setup Guide - Personal Finance Analytics

## 1. 📁 Directory Structure erstellen

```bash
# In deinem Projekt-Root (/c/dev/finance-analytics)
mkdir -p docker/init
mkdir -p docker/pgadmin
mkdir -p data/imports
mkdir -p data/exports
mkdir -p logs
```

## 2. 🗄️ Dateien Migration - Schritt für Schritt

### A) PostgreSQL Schema migrieren
```bash
# Kopiere dein vorhandenes postgres_schema.sql nach:
cp postgres_schema.sql docker/init/01-schema.sql

# Kopiere Test-Daten (optional)
cp data-h2.sql docker/init/02-seed-data.sql
```

### B) Docker-Dateien erstellen
Erstelle diese Dateien in deinem Projekt-Root:

1. **`docker-compose.yml`** - Haupt-Konfiguration (PostgreSQL 17, pgAdmin, optional Spring Boot)
2. **`docker-compose.override.yml`** - Development-Einstellungen (automatisch geladen)
3. **`.dockerignore`** - Optimiert Build-Performance
4. **`docker/pgadmin/servers.json`** - Pre-konfigurierte pgAdmin Verbindung
5. **`src/main/resources/application-docker.yml`** - Spring Boot Docker-Profil

### C) Lokale PostgreSQL stoppen (Port 5432 freigeben)
```bash
# Option 1: Windows Service stoppen
net stop postgresql-x64-17

# Option 2: Port in docker-compose.yml ändern
# "5433:5432" statt "5432:5432"
```

## 3. 🚀 Docker Workflow - Detailliert

### Development Modus (Empfohlen)
```bash
# 1. PostgreSQL + pgAdmin starten
docker-compose up postgres pgadmin

# 2. Spring Boot läuft weiterhin lokal
./gradlew bootRun --args='--spring.profiles.active=docker'

# 3. pgAdmin öffnen: http://localhost:8081
#    Email: dev@finance.local, Password: dev123
```

### Verschiedene Start-Modi
```bash
# Nur PostgreSQL (minimalistisch)
docker-compose up postgres

# Mit pgAdmin (empfohlen für Development)
docker-compose up postgres pgadmin

# Mit zusätzlichen Services (Redis, MailHog)
docker-compose --profile full-stack up

# Vollständig containerisiert (später)
docker-compose --profile app up banking-app
```

### Container Management
```bash
# Herunterfahren (behält Daten)
docker-compose down

# Datenbank RESET (ACHTUNG: Löscht alle Daten!)
docker-compose down -v

# Logs anschauen
docker-compose logs postgres
docker-compose logs -f pgadmin  # Follow logs

# Container Status
docker-compose ps

# In PostgreSQL einloggen
docker-compose exec postgres psql -U finance_user -d finance_analytics
```

## 4. 🔧 Spring Boot Integration - Detailliert

### A) application-docker.yml Konfiguration
```yaml
# Wichtige Einstellungen für Docker:
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/finance_analytics  # Container-Name!
    username: finance_user
    password: secure_password_123
  jpa:
    hibernate:
      ddl-auto: validate  # WICHTIG: validate statt update!
    show-sql: false  # Logging in Docker optimiert
```

### B) Profile-basierte Entwicklung
```bash
# Development mit Docker-PostgreSQL
./gradlew bootRun --args='--spring.profiles.active=docker'

# Development mit erweiterten Logs
./gradlew bootRun --args='--spring.profiles.active=docker,dev'

# Tests mit Testcontainers (nutzen dasselbe Image)
./gradlew test
```

### C) Testcontainers Integration
```java
@Testcontainers
class BankingDataIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_finance")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("docker/init/01-schema.sql");  // Gleiches Schema!
            
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

## 5. 🎯 Database Management - Detailliert

### pgAdmin Web Interface
- **URL**: http://localhost:8081
- **Login**: dev@finance.local / dev123
- **Pre-konfiguriert**: Verbindung zu finance_analytics bereits eingerichtet

### Direct Database Access
```bash
# Via Docker Compose
docker-compose exec postgres psql -U finance_user -d finance_analytics

# Via lokale psql (falls installiert)
psql -h localhost -p 5432 -U finance_user -d finance_analytics

# Nützliche SQL Commands:
\dt          # Tabellen anzeigen
\d transactions  # Schema der transactions Tabelle
SELECT COUNT(*) FROM transactions;  # Daten prüfen
```

### DBeaver Integration
```
Host: localhost
Port: 5432
Database: finance_analytics  
Username: finance_user
Password: secure_password_123
```

## 6. ✅ Vorteile dieses Docker-Setups

### Development Advantages
- **Konsistente Umgebung**: PostgreSQL 17 Alpine - lightweight & schnell
- **Hot Reload**: Spring Boot DevTools funktioniert weiterhin
- **Team-Ready**: `git clone` → `docker-compose up` → fertig
- **Port Flexibility**: Bei Konflikten einfach auf 5433:5432 ändern

### Testing Advantages  
- **Testcontainers**: Gleiches PostgreSQL Image wie Development
- **Integration Tests**: Echte Datenbank, isolierte Umgebung
- **CI/CD Ready**: GitHub Actions nutzt dasselbe Setup
- **Schema Validation**: ddl-auto=validate verhindert Schema-Drift

### Production Advantages
- **Production-Similar**: Container-Setup wie später in Production
- **Monitoring Ready**: Actuator Endpoints für Prometheus
- **Security Optimized**: Non-root User, Health Checks
- **Resource Management**: Memory Limits, Connection Pools

## 7. 🚨 Troubleshooting & Pro-Tips

### Häufige Probleme
```bash
# Port 5432 bereits belegt?
docker-compose -f docker-compose.yml up postgres  # Nutzt 5432
# ODER ändere in docker-compose.yml: "5433:5432"

# Container läuft nicht?
docker-compose logs postgres

# Datenbank zurücksetzen?
docker-compose down -v  # ACHTUNG: Löscht ALLE Daten!

# Permission Errors (Windows)?
# Stelle sicher dass Docker Desktop läuft und WSL2 aktiviert ist
```

### Development Workflow Optimierung
```bash
# 1. Morning Startup
docker-compose up -d postgres pgadmin  # Background
./gradlew bootRun --args='--spring.profiles.active=docker'

# 2. Database Changes
# Ändere docker/init/01-schema.sql
docker-compose restart postgres  # Lädt Schema neu

# 3. Clean Restart
docker-compose down && docker-compose up postgres pgadmin

# 4. Evening Shutdown (optional)
docker-compose down  # Daten bleiben erhalten
```

### Performance Optimierung
- **WSL2**: Für Windows - deutlich bessere I/O Performance
- **Docker Desktop Settings**: Mindestens 4GB RAM, 2 CPU Cores
- **Volume Performance**: Named Volumes sind schneller als Bind Mounts
- **Connection Pooling**: HikariCP ist bereits optimal konfiguriert