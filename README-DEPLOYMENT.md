# Deployment Guide - Full Stack Catalog Application

This guide explains how to build and deploy the full-stack catalog application as a single JAR file.

## Build Process Overview

The Maven build process automatically:
1. Installs Node.js and npm
2. Installs React dependencies
3. Builds the React application into static files
4. Copies the static files into Spring Boot's resources
5. Packages everything into a single executable JAR

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 (for production)

## Development Build

For development with hot-reload:

```bash
# Terminal 1: Start Spring Boot backend
cd backend
mvn spring-boot:run

# Terminal 2: Start React frontend
npm run dev
```

Frontend: http://localhost:5173
Backend API: http://localhost:8080/api

## Production Build

### 1. Build the Complete Application

```bash
cd backend
mvn clean package -Pproduction
```

This command will:
- Clean previous builds
- Install Node.js and npm
- Build the React frontend
- Copy static files to Spring Boot
- Create an executable JAR file

### 2. Run the Application

```bash
java -jar target/catalog-backend-1.0.0.jar
```

The complete application will be available at: http://localhost:8080

## Database Setup

### MySQL Configuration

1. **Install MySQL 8.0** and start the service

2. **Create database** (optional - Spring Boot creates it automatically):
```sql
CREATE DATABASE catalog_db;
```

3. **Update credentials** in `application-production.properties`:
```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### Sample Data

The application automatically creates:
- Product catalog with sample items
- Admin user: `admin` / `admin123`
- Regular user: `user` / `user123`

## Deployment Options

### 1. Standalone JAR Deployment

```bash
# Build
mvn clean package -Pproduction

# Deploy
java -jar target/catalog-backend-1.0.0.jar
```

### 2. Docker Deployment

Create `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/catalog-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
docker build -t catalog-app .
docker run -p 8080:8080 catalog-app
```

### 3. Docker Compose with MySQL

Create `docker-compose.yml`:
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: catalog_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
  
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/catalog_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
      SPRING_PROFILES_ACTIVE: production

volumes:
  mysql_data:
```

Run with:
```bash
docker-compose up -d
```

## Production Configuration

### Environment Variables

Override configuration using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://your-host:3306/catalog_db
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
export SERVER_PORT=8080

java -jar target/catalog-backend-1.0.0.jar
```

### JVM Options

For production deployment:

```bash
java -Xms512m -Xmx1024m -jar target/catalog-backend-1.0.0.jar
```

### SSL Configuration

Add SSL certificate for HTTPS:

```properties
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=password
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=catalog
server.port=8443
```

## File Structure After Build

```
backend/target/
├── classes/
│   ├── static/           # React build files
│   │   ├── index.html
│   │   └── assets/
│   ├── application.properties
│   └── com/catalog/      # Java classes
└── catalog-backend-1.0.0.jar  # Executable JAR
```

## Troubleshooting

### Build Issues

1. **Node.js installation fails**:
   - Check internet connection
   - Clear Maven cache: `mvn dependency:purge-local-repository`

2. **React build fails**:
   - Ensure all dependencies are in package.json
   - Check for TypeScript errors

3. **Static files not found**:
   - Verify files are in `target/classes/static/`
   - Check WebConfig.java resource handlers

### Runtime Issues

1. **Database connection fails**:
   - Verify MySQL is running
   - Check connection string and credentials
   - Ensure database exists

2. **API endpoints return 404**:
   - Check controller mappings
   - Verify CORS configuration

3. **Frontend routes not working**:
   - Ensure HomeController forwards SPA routes
   - Check static resource configuration

## Performance Optimization

### Production Settings

```properties
# Enable compression
server.compression.enabled=true

# Cache static resources
spring.web.resources.cache.cachecontrol.max-age=31536000

# Database connection pooling
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

### JVM Tuning

```bash
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xms512m -Xmx1024m -jar app.jar
```

## Monitoring

Add actuator for health checks:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Health endpoint: http://localhost:8080/actuator/health

## Security Considerations

1. **Change default passwords** in production
2. **Use environment variables** for sensitive data
3. **Enable HTTPS** for production
4. **Configure firewall** to restrict database access
5. **Regular security updates** for dependencies

## Backup Strategy

1. **Database backups**:
```bash
mysqldump -u root -p catalog_db > backup.sql
```

2. **Application backups**:
   - Store JAR files with version tags
   - Backup configuration files
   - Document environment variables