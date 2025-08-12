# Personal Finance Analytics Platform

> **Multi-Format Banking Data Processing & Analytics Engine** - Intelligent processing of banking data with interactive visualizations and automated transaction categorization

[![Java](https://img.shields.io/badge/Java-24-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-brightgreen.svg)](https://gradle.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 🎯 Overview

A comprehensive **Personal Finance Analytics Platform** that processes real banking data from multiple formats and provides intelligent insights through interactive visualizations. Built as a production-ready application with enterprise-grade patterns and architecture.

**Key Highlights:**
- 📊 Multi-format banking data import (CSV, PDF, MT940, CAMT.053)
- 🤖 Intelligent automatic transaction categorization
- 📈 Interactive data visualization dashboard
- 🏗️ 15+ design patterns implementation
- 🏦 Real-world banking domain expertise
- ✅ Production-tested with real family financial data

## ✨ Features

### 🏦 Banking Data Processing
- **Multi-Format Support**: CSV, PDF, MT940, CAMT.053
- **Auto-Detection**: Automatic bank format recognition for major German banks
- **Smart Categorization**: AI-powered transaction classification
- **Data Validation**: Robust error handling and data consistency checks

### 📊 Analytics & Visualization
- **Interactive Dashboard**: Real-time financial overview
- **Multiple Chart Types**: Pie, bar, and line charts with Chart.js
- **Trend Analysis**: Monthly/yearly comparisons and spending patterns
- **Budget Insights**: Savings potential identification
- **Anomaly Detection**: Unusual spending pattern alerts

### 🎨 Architecture Showcase
- **Design Patterns**: 15+ patterns including Strategy, Factory, Observer, Chain of Responsibility
- **Clean Architecture**: SOLID principles with clear separation of concerns
- **Scalable Design**: Extensible for additional banks and formats
- **Enterprise Patterns**: Repository, Service, and MVC patterns

## 🚀 Quick Start

### Prerequisites
```bash
Java 24+
PostgreSQL 17+
Gradle 8.x
```

### Installation
```bash
# Clone the repository
git clone https://github.com/your-username/personal-finance-analytics.git
cd personal-finance-analytics

# Set up database
createdb finance_analytics

# Configure application properties
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Edit database connection settings

# Run the application
./gradlew bootRun
```

### First Steps
1. Open your browser to `http://localhost:8080`
2. Upload your bank's CSV export file
3. Explore automatic categorization and visualizations
4. Set up budget goals and track spending trends

## 🏗️ Tech Stack

### Backend (Core Focus)
- **Spring Boot 3.5.4** - Enterprise Java framework
- **Java 24** - Latest stable Java version
- **PostgreSQL 17** - Robust ACID-compliant database
- **Apache POI 5.x** - PDF processing capabilities
- **Apache Commons CSV** - CSV parsing with auto-detection
- **Jackson** - JSON processing extensions
- **Testcontainers** - Integration testing

### Frontend (Visualization)
- **Thymeleaf** - Server-side templating
- **Bootstrap 5** - Responsive UI framework
- **Chart.js 4.x** - Interactive data visualizations
- **Responsive Design** - Mobile-optimized interface

### Supported Banking Formats

| Format | Status | Description |
|--------|--------|-------------|
| CSV | 🔄 In Progress | Auto-detection for major German banks |
| MT940 | 🔄 In Progress | Standardized SWIFT format |
| PDF | 🔄 In Progress | Text-based bank statements |
| CAMT.053 | 📋 Planned | ISO 20022 standard |

#### Supported Banks (CSV Auto-Detection)
- **Sparkasse** - Germany's largest banking group
- **Volksbank** - Cooperative banking network
- **Deutsche Bank** - Major commercial bank
- **DKB** - Direct banking services
- **ING** - International banking group

## 🎨 Design Patterns Implementation

### Creational Patterns
- **Factory Method**: Bank data importer creation
- **Abstract Factory**: Chart generation for different visualizations
- **Builder Pattern**: Complex financial query construction
- **Singleton**: Spring-managed service beans

### Structural Patterns
- **Adapter**: Legacy bank format integration
- **Facade**: Simplified banking API interface
- **Decorator**: Enhanced interactive chart features
- **Composite**: Hierarchical category structures

### Behavioral Patterns
- **Strategy**: Multi-format import pipeline
- **Chain of Responsibility**: Transaction categorization pipeline
- **Command**: Analysis operations and commands
- **Observer**: Budget alerts and notifications
- **State**: Transaction processing lifecycle
- **Template Method**: Common analytics operations
- **Visitor**: Extensible services without modifying core entities

## 📁 Project Structure

```
src/main/java/de/finance/analytics/
├── FinanceAnalyticsApplication.java    # Main application entry point
├── banking/
│   ├── importer/                       # Multi-format import logic
│   ├── detector/                       # Bank format auto-detection
│   └── parser/                         # Bank-specific data parsers
├── transaction/
│   ├── entity/                         # JPA domain entities
│   ├── repository/                     # Data access layer
│   └── service/                        # Business logic services
├── analytics/
│   ├── service/                        # Analytics computation engine
│   └── patterns/                       # Design patterns showcase
├── web/
│   └── controller/                     # REST & MVC controllers
└── config/                             # Application configuration

src/main/resources/
├── templates/                          # Thymeleaf HTML templates
├── static/                             # CSS, JS, images
└── application.properties              # Configuration settings
```

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run with coverage report
./gradlew test jacocoTestReport

# Integration tests with Testcontainers
./gradlew integrationTest
```

## 📊 Usage Examples

### Uploading Bank Data
```java
// Example: CSV file upload and processing
@PostMapping("/upload")
public String uploadBankData(@RequestParam("file") MultipartFile file) {
    BankDataImporter importer = importerFactory.createImporter(file);
    List<Transaction> transactions = importer.importData(file);
    transactionService.saveAll(transactions);
    return "redirect:/dashboard";
}
```

### Querying Transactions
```java
// Example: Building complex financial queries
TransactionQuery query = TransactionQuery.builder()
    .dateRange(startDate, endDate)
    .categories(Arrays.asList("Groceries", "Transportation"))
    .minAmount(new BigDecimal("50.00"))
    .transactionType(TransactionType.DEBIT)
    .build();
```

## 🤝 Contributing

While this is primarily a portfolio project, contributions are welcome! Please feel free to:

1. **Report Issues**: Found a bug? Open an issue with detailed information
2. **Suggest Features**: Have ideas for new banking formats or features?
3. **Submit Pull Requests**: Code improvements and new features are appreciated

### Development Setup
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📋 Roadmap

### Version 0.9 (Current)
- [x] Building foundation

### Version 1.0 (In Progress)
- [ ] CSV auto-detection for major banks
- [ ] Basic transaction categorization
- [ ] Interactive dashboard with charts
- [ ] PostgreSQL integration

### Version 1.1 (Planned)
- [ ] MT940 format support
- [ ] PDF statement parsing
- [ ] Advanced analytics features
- [ ] Budget goal setting

### Version 2.0 (Planned)
- [ ] CAMT.053 ISO standard support
- [ ] Machine learning categorization
- [ ] REST API for external integrations
- [ ] Mobile application

## ❓ FAQ

**Q: Is my financial data secure?**
A: Yes, all data is processed locally. The application doesn't send data to external services.

**Q: Which banks are supported?**
A: Currently supports CSV exports from major German banks. MT940 support is universal.

**Q: Can I add custom categories?**
A: Yes, the application supports custom category creation and modification.

**Q: Is there an API available?**
A: REST API documentation is planned for version 1.1.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Chart.js community for visualization components
- PostgreSQL community for the robust database system
- Banking standards organizations for format specifications

## 📞 Contact

**Project Maintainer**: [Your Name]
- 💼 LinkedIn: [Your LinkedIn Profile](https://linkedin.com/in/your-profile) (Comming soon ...)
- 💼 Xing: [Alexander Frömberg](https://www.xing.com/profile/Alexander_Froemberg2/web_profiles)
- 📧 Email: alexander.froemberg@tu-dortmund.de
- 🐙 GitHub: [AlexanderF87](https://github.com/AlexanderF87/)

---

⭐ **Star this repository if you found it helpful!** ⭐

*Built with ❤️ for the Banking & Fintech community*