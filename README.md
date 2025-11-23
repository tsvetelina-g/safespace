# SafeSpace 🛡️

A hackathon MVP for a school bullying reporting system that enables students to report incidents anonymously and helps school staff identify and respond to bullying situations quickly.

## ⚠️ Project Status

**This is a HACKATHON MVP - NOT PRODUCTION READY**

This project was developed during a hackathon as a proof of concept. It contains unfinished features, may have security vulnerabilities, and should not be deployed to production environments without significant review and improvements.

## 🎯 Overview

SafeSpace is a web-based platform that allows students to report bullying incidents (anonymously or with their account). The system uses AI to analyze reports, provide immediate supportive responses to students, and helps teachers manage and respond to incidents effectively.

### Key Features

- **Anonymous Reporting**: Students can report incidents without revealing their identity
- **Authenticated Reporting**: Registered students can report with their account information
- **AI-Powered Analysis**: Reports are automatically analyzed using OpenAI to:
  - Classify the type and source of the incident
  - Assess urgency, severity, importance, and credibility
  - Generate supportive, age-appropriate responses for students (in Bulgarian)
  - Recommend appropriate actions for school staff
- **Teacher Dashboard**: Teachers can view, filter, and manage reports
- **Role-Based Access**: Separate access for students and teachers
- **Secure Authentication**: Spring Security integration for user authentication

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.4.0
- **Database**: MySQL 8
- **Frontend**: Thymeleaf templates, HTML, CSS
- **Security**: Spring Security
- **AI Integration**: OpenAI API
- **Java Version**: 17
- **Build Tool**: Maven

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- OpenAI API key

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd safespace
```

### 2. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE school_bully;
```

Or the database will be created automatically if you have the proper MySQL configuration.

### 3. Configuration

Update `src/main/resources/application.properties` with your database credentials and OpenAI API key:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/school_bully?createDatabaseIfNotExist=true
spring.datasource.username=your_username
spring.datasource.password=your_password
openai.api.key=your_openai_api_key
```

### 4. Run the Application

Using Maven:

```bash
./mvnw spring-boot:run
```

Or on Windows:

```bash
mvnw.cmd spring-boot:run
```

The application will start on `http://localhost:8086`

### 5. Default Users

The application includes initial data setup (see `DataInitializer.java`). Default users are created on startup:
- Student accounts (role: `STUDENT`)
- Teacher accounts (role: `TEACHER`)

Check the `DataInitializer` class for default credentials.

## 📱 Usage

### For Students

1. Navigate to `/signal` to report an incident
2. Describe the situation in the text area
3. Choose whether to report anonymously
4. Receive immediate AI-generated supportive response

### For Teachers

1. Log in with teacher credentials
2. Navigate to `/teacher` dashboard
3. View all reports or filter by:
   - All reports
   - Unprocessed reports
   - Processed reports
4. Mark reports as notified when appropriate action has been taken

## 📁 Project Structure

```
src/main/java/app/schoolbully/
├── config/           # Configuration classes
├── model/
│   ├── entity/       # JPA entities (User, Signal)
│   └── enums/        # Enumeration types
├── repository/       # Data access layer
├── security/         # Spring Security configuration
├── service/          # Business logic
└── web/              # Controllers

src/main/resources/
├── static/           # CSS and static assets
├── templates/        # Thymeleaf HTML templates
└── application.properties
```

## 🔒 Security Notes

⚠️ **IMPORTANT**: This is a hackathon MVP. Security measures are minimal and may contain vulnerabilities:

- Passwords should be properly hashed in production
- API keys are stored in plain text (use environment variables or secrets management)
- Input validation needs strengthening
- SQL injection prevention needs review
- Rate limiting is not implemented
- HTTPS is not configured

## 🚧 Known Limitations

- AI analysis may not always be accurate
- No email notifications
- No SMS integration
- Limited user management interface
- No reporting/analytics dashboard
- Basic error handling
- Limited input validation
- No file upload support

## 🎓 Hackathon Context

This project was developed as part of a hackathon focused on addressing school safety and bullying prevention. The goal was to create a working MVP that demonstrates:

1. The concept of anonymous reporting
2. AI-assisted analysis of incidents
3. A simple interface for both students and teachers
4. Rapid response to student reports

## 📝 License

This project is a hackathon MVP. Please review licensing requirements before using in production.

## 👥 Contributors

Hackathon team members

## 🤝 Contributing

This is a hackathon project. If you'd like to improve it, please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## ⚠️ Disclaimer

This software is provided "as is" without warranty of any kind. Use at your own risk. This hackathon MVP should not be used in production environments without proper security audits, testing, and compliance reviews.

