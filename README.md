# EWF Service - Portal Application
Welcome to the **EWF Portal** application! This is a modern, production-ready web application accessible via [ewfportal.com](https://ewfportal.com). It is designed and developed using **Jakarta EE**, **Spring Framework**, and other state-of-the-art technologies to provide secure, reliable, and user-friendly services.
## Features
1. **User Authentication & Authorization**
    - Secure login and role-based access using **JWT Tokens**.

2. **Database-Driven Functionality**
    - Uses **MySQL** as the database, integrated with **Spring Data JPA** for robust data management.

3. **RESTful APIs**
    - APIs designed using **Spring REST MVC**, enabling clear interaction between front-end and back-end systems.

4. **Caching**
    - Simple, fast, and configurable caching with **Spring Cache**.

5. **Deployment Automation**
    - Deployment to the production server is automated via **GitHub Actions**, ensuring a seamless CI/CD pipeline.

6. **Platform Independent**
    - Runs on any **Java 17-compatible** platform with no dependency on specific server-side resources.

## Technology Stack
- **Programming Language**: Java 17
- **Frameworks**:
    - Jakarta EE (for foundational enterprise support)
    - Spring Boot, Spring MVC, Spring Data JPA

- **Databases**:
    - MySQL (with Hibernate ORM)

- **API Authentication**: JSON Web Tokens (JWT)
- **Build Tools**: Maven
- **Version Control**: Git + GitHub
- **CI/CD**: GitHub Actions with workflow automation
- **Deployment**: Ubuntu-based VPS server
- **Containerization (Optional)**: Docker-compatible
- **Log Management**: Application-specific logging with configurable verbosity levels

## Environment Setup
1. **Pre-requisites**:
    - Install **Java 17** or higher.
    - Install **MySQL** (or configure appropriate database access).
    - Install **Maven** for package management.
    - Configure environment variables (as used in deployment):
        - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
        - `JWT_SECRET`, `SEARCHAPI_TOKEN`

2. **Run Locally**:
``` bash
   mvn spring-boot:run
```
1. **Default Configuration**:
    - The application runs on **`localhost:8080`** by default.
    - Update **`src/main/resources/application.properties`** to match your database and other configurations.

## Deployment Process
This project uses **GitHub Actions** for automated deployment upon changes applied to the `master` branch.
- **CI/CD Pipeline Overview**:
    - **Test job**: Runs all unit tests to ensure code is stable.
    - **Build job**: Packages the application into a deployable JAR file.
    - **Copy job**: Transfers the JAR to the production VPS.
    - **Deploy job**:
        - Stops the running application (if applicable).
        - Replaces the JAR file on the server.
        - Starts the application.

- **Production Deployment Server**:
    - Hosted on [ewfportal.com](https://ewfportal.com).
    - Runs on **Ubuntu** (VPS).
    - Using **SSH** for secure file transfers and command execution.

## File Structure
``` plaintext
.
├── src
│   ├── main
│   │   ├── java              # Java source files
│   │   ├── resources         # Configuration files (application.properties, etc.)
│   └── test                  # Test cases for unit testing
├── pom.xml                   # Maven build configuration
├── .github/workflows         # CI/CD workflows
└── README.md                 # Project documentation
```
## Configuration Details
1. **Application Properties**:
    - Located in `src/main/resources/application.properties`.
    - Includes configuration for:
        - Database
        - Server (port, address)
        - JWT and API tokens
        - Logging levels

2. **Environment Variables**:
    - Add the following environment variables to your deployment server:
        - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
        - `JWT_SECRET`, `SEARCHAPI_TOKEN`

3. **Secrets**:
    - Secrets for GitHub Actions are securely stored in the repository settings.

## Testing
Run unit tests before deployment to ensure stability:
``` bash
mvn test
```
## Known Issues & Future Improvements
1. **Scalability**:
    - Explore containerized deployment (e.g., Docker, Kubernetes).

2. **Monitoring**:
    - Add monitoring tools like **Prometheus** or **Grafana** for production environments.

3. **Logging**:
    - Integrate **centralized log management** for production (e.g., ELK stack).

4. **Documentation**:
    - Automatically generate API documentation using tools like **Swagger**.

## Links
- **Website**: [ewfportal.com](https://ewfportal.com)
- **Repository**: [GitHub Repository](https://github.com/thanhnguyen0311/ewf-service)
