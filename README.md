# Personal Finance Analytics Platform

> **Banking Data Processing & Analytics Engine** - Intelligente Verarbeitung von Multi-Format Bankdaten mit interaktiven Visualisierungen

[![Java](https://img.shields.io/badge/Java-24-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-brightgreen.svg)](https://gradle.org/)

## 🎯 Project Vision

**Portfolio-Projekt für Senior Java Developer Position (Banking/Fintech)**
- Multi-Format Banking Data Import Pipeline (CSV, PDF, MT940, CAMT.053)
- Intelligente automatische Transaktions-Kategorisierung
- Interactive Data Visualization Dashboard
- 15+ Design Patterns in Financial Context
- Production-Ready: Familie nutzt App mit echten Daten

## 🏗️ Architecture & Tech Stack

### Backend (80% Focus)
```
Spring Boot 3.5.4 + Banking Data Processing
├── Multi-Format Import Pipeline
├── PostgreSQL + Financial Data Schema  
├── Apache POI (PDF Processing)
├── Apache Commons CSV (Auto-Detection)
└── 15+ Design Patterns Implementation
```

### Frontend (20% Visualization)
```
Thymeleaf + Bootstrap + Chart.js
├── Responsive Dashboard
├── Interactive Charts (Pie/Bar/Line)
├── Real-time Analytics
└── Mobile-Optimized
```

## 🏦 Banking Data Support

### Supported Formats
- ✅ **CSV Auto-Detection** (Top 5 German Banks)
- 🔄 **MT940** (Standardized Banking Format)
- 🔄 **PDF Text-Parsing** (Known Bank Layouts)
- 📋 **CAMT.053** (ISO 20022 Standard)

### Auto-Detection Banks
```java
SPARKASSE("Auftragskonto", "Buchungstag", "Verwendungszweck", "Betrag")
VOLKSBANK("Buchungstag", "Auftraggeber/Empfänger", "Vorgang", "Umsatz") 
DEUTSCHE_BANK("Buchungstag", "Wert", "Begünstigter", "Betrag (EUR)")
DKB("Buchungsdatum", "Auftraggeber / Begünstigter", "Betrag (EUR)")
ING("Buchung", "Name Zahlungsbeteiligter", "Betrag")
```

## 🎨 Design Patterns Showcase

### Data Processing Patterns
- **Strategy Pattern**: Multi-Format Import Pipeline
- **Factory Pattern**: Bank Format Detection & Parser Creation
- **Chain of Responsibility**: Transaction Categorization Pipeline
- **Command Pattern**: Analysis Commands & Operations

### Visualization Patterns
- **Abstract Factory**: Chart Creation (Pie/Bar/Line)
- **Decorator Pattern**: Interactive Chart Enhancement
- **Observer Pattern**: Real-time Budget Alerts

### Business Logic Patterns
- **Builder Pattern**: Complex Financial Query Construction
- **Template Method**: Common Analytics Operations
- **Facade Pattern**: Simplified Banking API

## 🚀 Quick Start

### Prerequisites
- Java 24+
- PostgreSQL 17+
- Gradle 8.x

### Installation
```bash
# Clone repository
git clone https://github.com/your-username/personal-finance-analytics.git
cd personal-finance-analytics

# Setup database (PostgreSQL must be running)
createdb finance_analytics

# Run application
./gradlew bootRun
```

### First Run
1. Navigate to `http://localhost:8080`
2. Upload your bank's CSV export
3. Enjoy automatic categorization & visualizations!

## 📊 Features & Screenshots

### Dashboard Overview
![Dashboard](docs/images/dashboard.png)
*Real-time financial overview with category breakdown*

### Multi-Format Import
![Import](docs/images/import-pipeline.png)
*Automatic bank detection and data processing*

### Analytics & Trends
![Analytics](docs/images/analytics.png)
*Interactive trend analysis and budget insights*

## 🛠️ Development

### Project Structure
```
src/main/java/de/finance/analytics/
├── FinanceAnalyticsApplication.java
├── banking/
│   ├── importer/     # Multi-format import logic
│   ├── detector/     # CSV auto-detection
│   └── parser/       # Bank-specific parsers
├── transaction/
│   ├── entity/       # JPA entities
│   ├── repository/   # Data access layer
│   └── service/      # Business logic
├── analytics/
│   ├── service/      # Analytics engine
│   └── patterns/     # Design patterns showcase
└── web/
    └── controller/   # REST & MVC controllers
```

### Development Philosophy
> **"Start Simple, Build Extensible, Iterate Fast"**

- MVP-First Approach: Funktionsfähig in Wochen, nicht Monaten
- Pattern Evolution: Organic growth, avoid over-engineering
- Real Usage: Familie nutzt App mit echten Daten ab Tag 1
- Same Data, Different Views: Analytics logic once, visualization flexible

## 📈 MVP Timeline (3 Weeks)

### Week 1: Foundation
- [x] Spring Boot Setup + Banking Package Structure
- [x] CSV Auto-Detection (Top 5 Banks)
- [x] Transaction Entity + Database Schema
- [x] Basic Categorization Logic
- [x] Thymeleaf Dashboard + Chart.js Pie Chart
- **Result:** Familie kann echte Bankdaten visualisieren

### Week 2: Enhancement
- [ ] PDF Text-Parsing (Known Bank Layouts)
- [ ] Strategy Pattern Implementation
- [ ] Line Chart für Trends über Zeit
- [ ] Enhanced Analytics Service
- **Result:** 2 Formats, 2 Charts, Patterns demonstriert

### Week 3: Production Polish
- [ ] MT940 Standard-Import
- [ ] Bar Chart für Kategorie-Vergleiche
- [ ] UI Polish + Error Handling
- [ ] Production-Ready Deployment
- **Result:** Demo-ready App mit Pattern-Showcase

## 🎯 Business Value

### Family Use Cases
- **Dashboard:** Einnahmen vs. Ausgaben mit Ersparnis-Rate
- **Kategorien:** Wohnen, Lebensmittel, Transport, Freizeit
- **Filter:** Zeitraum, Kategorien, Mindestbetrag
- **Analytics:** Monats-/Jahresvergleiche, Spar-Potential
- **Trends:** Ungewöhnliche Ausgaben-Muster erkennen

### Portfolio Impact
- **Technical:** Multi-format data processing expertise
- **Business:** Financial domain knowledge demonstration
- **Patterns:** 15+ design patterns in practical context
- **Production:** Real-world usage validation

## 🔧 API Documentation

*Coming Soon: Swagger UI integration for REST API exploration*

## 🤝 Contributing

This is a portfolio project, but feedback and suggestions are welcome!

## 📝 License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## 📧 Contact

**[Your Name]** - Banking/Fintech Java Developer
- Portfolio: [your-portfolio.com](https://your-portfolio.com)
- LinkedIn: [your-linkedin](https://linkedin.com/in/your-profile)
- Email: your.email@domain.com

---
*Built with ❤️ for the Banking/Fintech community*