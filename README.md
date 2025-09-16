<div align="center">
  <h2>
    💼 Portal Laboral - Job Portal Platform
  </h2>
</div>

<div align="center">
    <img alt="Static Badge" src="https://img.shields.io/badge/version-1.0-blue">
    <img alt="Static Badge" src="https://img.shields.io/badge/Spring Boot-3.4.1-green">
    <img alt="Static Badge" src="https://img.shields.io/badge/Java-17-orange">
    <img alt="Static Badge" src="https://img.shields.io/badge/Angular-18+-red">
    <img alt="Static Badge" src="https://img.shields.io/badge/License-MIT-lightgreen">
</div>
<br>

<div align="center">
<h2>
🔧 Technologies Used
</h2>
</div>

<div align="center">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" height="40" alt="Spring Logo" />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" height="40" alt="Java Logo" />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/angular/angular-original.svg" height="40" alt="Angular Logo" />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/typescript/typescript-original.svg" height="40" alt="TypeScript Logo" />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg" height="40" alt="Docker Logo" />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/microsoftsqlserver/microsoftsqlserver-original.svg" height="40" />       
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg" height="40" alt="Git Logo" />
  <img width="12" />        
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/github/github-original.svg" height="40" alt="GitHub Logo" />
</div>

# Microservices Job Portal Platform

**Portal Laboral** is a comprehensive job portal platform built with microservices architecture, designed to connect job seekers, companies, and administrators in a modern, scalable environment. This project implements **Hexagonal Architecture** and **BFF (Backend for Frontend)** patterns, utilizing cutting-edge technologies like **Spring Boot**, **Angular**, and **Docker**.

The platform enables complete job management through CRUD operations, advanced filtering, document management, and real-time application tracking, implementing industry best practices and modern development tools.

---

## Features

### 👤 **For Job Seekers**
- User registration and secure authentication with JWT
- Document management (PDF CV, certificates upload)
- Profile customization with photo upload
- Advanced job search with filters (experience, location, salary, contract type)
- One-click application using saved CV
- Application history and status tracking (under review, interview, rejected)

### 🏢 **For Companies**
- Company profile management with logo and description
- Job posting creation and editing
- Requirement definition, salary, and benefits setup
- Publication scheduling with expiration dates
- Candidate management with advanced filtering
- Applicant acceptance and hiring process

### 🛠️ **For Administrators**
- Complete account management (CRUD operations)
- Real-time dashboard with comprehensive statistics
- Active user metrics and hiring success rates
- Sector-based analytics and trends

---

## Architecture

The project follows **Hexagonal Architecture** with **microservices** pattern:

```
Frontend (Angular) → API Gateway → Microservices → SQL Server
```

**Microservices:**
- **Authentication Service**: JWT-based security and user management
- **Job Seekers Service**: Candidate profiles and applications
- **Companies Service**: Employer profiles and job postings
- **Administration Service**: System management and analytics

---

## Prerequisites
- **Java 17 or higher**
- **Node.js 18+**
- **Maven 3.8+**
- **Angular CLI 18+**
- **Docker & Docker Compose**
- **SQL Server Database**

---

## Installation and Configuration

1. **Clone Repository**
     
    ```bash
     git clone https://github.com/your-username/portal-laboral.git
     cd portal-laboral
    ```

2. **Configure Environment Variables**

Create a `.env` file in the root directory:

```properties
# Database Configuration
DB_HOST=localhost
DB_PORT=1433
DB_NAME=portal_laboral
DB_USER=sa
DB_PASSWORD=YourPassword123

# JWT Configuration
JWT_SECRET=your-super-secret-key-here
JWT_EXPIRATION=86400000

# CORS Configuration
ALLOWED_ORIGINS=http://localhost:4200

# Email Service (Future Implementation)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

Create a new database called `portal_laboral`:
```sql
CREATE DATABASE portal_laboral;
```

3. **Option A: Run with Docker (Recommended)**

     ```bash
     # Build and start all services
     docker-compose up --build
     
     # For development mode with hot-reload
     docker-compose -f docker-compose.dev.yml up
     ```

4. **Option B: Manual Installation**

**Backend Services:**
```bash
# Authentication Service
cd auth-service
./mvnw clean install
./mvnw spring-boot:run

# Repeat for other services
cd ../postulantes-service
./mvnw clean install
./mvnw spring-boot:run

cd ../empresas-service
./mvnw clean install
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd portal-frontend
npm install
ng serve

# For production build
ng build --prod
```

The application will be available on: 
- **Frontend**: `http://localhost:4200`
- **Auth Service**: `http://localhost:8080`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`

---

## Principal Endpoints

### **Authentication Service (Port 8080)**
- **Authentication**:
  - `POST /auth/login`: User login and JWT token retrieval
  - `POST /auth/registro/postulante`: Job seeker registration
  - `POST /auth/registro/empresa`: Company registration
  - `POST /auth/refresh`: Access token renewal
  - `POST /auth/logout`: User logout

### **Job Seekers Service (Port 8081)**
- **Profile Management**:
  - `GET /postulantes/profile`: Get user profile
  - `PUT /postulantes/profile`: Update profile information
  - `POST /postulantes/documents`: Upload CV and certificates
  - `GET /postulantes/applications`: Get application history

### **Companies Service (Port 8082)**
- **Job Management**:
  - `GET /empresas/jobs`: List company jobs
  - `POST /empresas/jobs`: Create new job posting
  - `PUT /empresas/jobs/{id}`: Update job posting
  - `DELETE /empresas/jobs/{id}`: Delete job posting
  - `GET /empresas/jobs/{id}/applicants`: Get job applicants

### **Administration Service (Port 8083)**
- **System Management**:
  - `GET /admin/users`: List all users
  - `GET /admin/statistics`: Get platform statistics
  - `PUT /admin/users/{id}/status`: Update user status

---

## API Documentation

Once services are running, access Swagger documentation:

- **Auth Service**: http://localhost:8080/swagger-ui.html
- **Job Seekers Service**: http://localhost:8081/swagger-ui.html
- **Companies Service**: http://localhost:8082/swagger-ui.html
- **Administration Service**: http://localhost:8083/swagger-ui.html

---

## Testing

**Backend Testing:**
```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw test -Dtest=**/*IntegrationTest

# Coverage report
./mvnw jacoco:report
```

**Frontend Testing:**
```bash
# Unit tests
ng test

# End-to-end tests
ng e2e

# Coverage report
ng test --code-coverage
```

---

## Project Structure

```
portal-laboral/
├── auth-service/              # Authentication microservice
├── postulantes-service/       # Job seekers microservice
├── empresas-service/          # Companies microservice
├── admin-service/             # Administration microservice
├── portal-frontend/           # Angular frontend application
├── docker-compose.yml         # Container orchestration
├── .github/workflows/         # CI/CD pipelines
└── README.md
```

---

## How to Contribute

Contributions are welcome! If you want to improve this project, follow these steps to contribute via GitHub:

1. **Fork the repository**:
   Click the "Fork" button at the top of the repository page.

2. **Clone your fork locally**:

   ```bash
   git clone https://github.com/your-username/portal-laboral.git
   cd portal-laboral
   ```

3. **Create a new branch for your feature**
   ```bash
   git checkout -b feature/new-feature
   ```

4. **Make your changes and commit with descriptive messages**
   ```bash
   git commit -m "Add: detailed description of what you've changed"
   ```

5. **Push your branch to the remote repository**
   ```bash
   git push origin feature/new-feature
   ```

6. **Open a Pull Request**
   - Go to the "Pull requests" tab in the original repository.
   - Click "New pull request" and select the branch you've pushed.
   - Provide a clear description of the changes you've made.

7. **Wait for the review** 
   Your Pull Request will be reviewed, and if everything is in order, it will be merged into the project's main branch.

### **Code Conventions**
- **Java**: Follow Google Java Style Guide
- **TypeScript/Angular**: Follow Angular Style Guide  
- **Commits**: Use Conventional Commits (feat:, fix:, docs:, etc.)
- **Testing**: Minimum 80% code coverage required

---

## Roadmap

### **v1.0 (Current)**
- [x] JWT Authentication system
- [x] Basic CRUD for job seekers and companies
- [ ] Complete frontend implementation
- [ ] Integration testing suite

### **v2.0 (Next Release)**
- [ ] Real-time notification system
- [ ] Live chat functionality
- [ ] API Gateway implementation
- [ ] Metrics and monitoring dashboard

### **v3.0 (Future)**
- [ ] Machine Learning for job matching
- [ ] Mobile application
- [ ] Social media integration
- [ ] Predictive analytics

---

### Contact

If you have any questions or suggestions, feel free to open an issue in the repository or contact us via markusperezch1@gmail.com.

---

## License
This project is licensed under the MIT License. See the `LICENSE` file for more details.

---

<div align="center">
  <p>⭐ <strong>If you like this project, give it a star on GitHub!</strong> ⭐</p>
</div>
