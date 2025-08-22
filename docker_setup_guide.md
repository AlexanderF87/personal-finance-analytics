# 🐳 Docker Setup Guide - Personal Finance Analytics

## 1. 📁 Directory Structure erstellen

```bash
# In deinem Projekt-Root (/c/dev/finance-analytics)
mkdir -p docker/init
mkdir -p docker/scripts
```

## 2. 🗄️ PostgreSQL Schema nach Docker migrieren

Kopiere dein vorhandenes `postgres_schema.sql` nach:
```
docker/init/01-schema.sql
```

Und `data-h2.sql` als Test-Daten nach:
```
docker/init/02-seed-data.sql
```

## 3. 📋 Notwendige Docker-Dateien erstellen

### A) `docker-compose.yml` (Haupt-Datei)
### B) `docker-compose.override.yml` (Development)
### C) `.dockerignore` 
### D) `Dockerfile` (für später)

## 4. 🚀 Docker Workflow

```bash
# 1. Nur PostgreSQL starten (Development)
docker-compose up postgres

# 2. Mit pgAdmin für DB-Management
docker-compose up postgres pgadmin

# 3. Alle Services (später mit Spring Boot Container)
docker-compose --profile full-stack up

# 4. Herunterfahren
docker-compose down

# 5. Datenbank zurücksetzen
docker-compose down -v  # Löscht auch Volumes!
```

## 5. 🔧 Integration mit deinem Spring Boot

### application-docker.yml erstellen:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finance_analytics
    username: finance_user  
    password: secure_password_123
  jpa:
    hibernate:
      ddl-auto: validate  # Wichtig: validate statt update!
```

### Testcontainers für Integration Tests:
```java
@Testcontainers
class BankingDataIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("test_finance")
            .withUsername("test")
            .withPassword("test");
}
```

## 6. ✅ Vorteile dieses Setups

- **Konsistente Umgebung**: Gleiche PostgreSQL Version überall
- **Einfache Tests**: Testcontainers nutzen dasselbe Docker Image  
- **Team-Ready**: Kollegen können sofort starten
- **CI/CD Ready**: GitHub Actions kann dasselbe Setup nutzen
- **Production-Similar**: Docker-Container wie in Production

## 7. 🎯 Deine nächsten Schritte

1. **Docker-Dateien erstellen** (ich helfe dabei)
2. **Lokale PostgreSQL stoppen** (Port 5432 freigeben)
3. **Docker-Container starten**
4. **Spring Boot Konfiguration anpassen**
5. **Tests mit Testcontainers erweitern**

## 8. 🔍 Database Management

- **pgAdmin**: http://localhost:8081
  - Email: admin@finance.local  
  - Password: admin123
- **Direct Connection**: 
  - Host: localhost:5432
  - DB: finance_analytics
  - User: finance_user

## 9. 💡 Pro-Tips

- **Volume Persistence**: Deine Daten bleiben erhalten zwischen Container-Neustarts
- **Hot Reload**: Spring Boot DevTools funktioniert weiterhin
- **Port Conflicts**: Falls 5432 belegt ist, ändere auf 5433:5432
- **Performance**: Docker Desktop auf Windows braucht WSL2 für beste Performance