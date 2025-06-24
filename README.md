# Catalog Website - Full Stack Application

A modern, responsive catalog website built with React frontend and Spring Boot backend with MySQL database.

## Features

### Frontend (React + TypeScript)
- ✨ Beautiful, responsive product catalog interface
- 🔍 Real-time search functionality
- 🏷️ Category-based filtering
- 📱 Mobile-first responsive design
- ⭐ Product ratings and reviews display
- 🛒 Add to cart functionality
- 💫 Smooth animations and hover effects

### Backend (Spring Boot + MySQL)
- 🚀 RESTful API with full CRUD operations
- 🗄️ MySQL database integration with JPA/Hibernate
- 📊 Advanced product search and filtering
- 🔐 Input validation and error handling
- 📈 Optimized database queries
- 🌐 CORS configuration for cross-origin requests

### Build System
- 📦 Maven configuration that builds React app into Spring Boot
- 🔧 Automated frontend build process
- 📁 Static resource serving from Spring Boot
- 🚀 Single JAR deployment

## Tech Stack

**Frontend:**
- React 18 with TypeScript
- Tailwind CSS for styling
- Lucide React for icons
- Vite for development server

**Backend:**
- Spring Boot 3.2
- Spring Data JPA
- MySQL 8.0
- Java 17

**Build:**
- Maven with Frontend Maven Plugin
- Node.js and npm integration

## Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- MySQL 8.0
- Maven 3.6 or higher

### Database Setup
1. **Install MySQL 8.0** and start the MySQL service

2. **Create MySQL database** (optional - Spring Boot will create it automatically):
```sql
CREATE DATABASE catalog_db;
```

3. **Update database credentials** in `backend/src/main/resources/application.properties`:
```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### Running the Application

#### Development Mode
1. **Start MySQL service** on your system

2. **Start the backend:**
```bash
cd backend
mvn spring-boot:run
```

3. **Start the frontend (in a separate terminal):**
```bash
npm run dev
```

The frontend will be available at `http://localhost:5173` and the backend API at `http://localhost:8080`

#### Production Build
Build and run as a single application:
```bash
cd backend
mvn clean package
java -jar target/catalog-backend-1.0.0.jar
```

The complete application will be available at `http://localhost:8080`

## Database Schema

The application automatically creates the following MySQL table structure:

```sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    image_url VARCHAR(1000),
    rating DECIMAL(2,1),
    in_stock BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

Sample data is automatically inserted on first run via `data.sql`.

## API Endpoints

### Products
- `GET /api/products` - Get all products (with optional query parameters)
  - `?category=electronics` - Filter by category
  - `?search=keyword` - Search products
  - `?sort=price_asc|price_desc|latest` - Sort products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `GET /api/products/categories` - Get all categories
- `GET /api/products/in-stock` - Get in-stock products

### Example API Usage
```bash
# Get all products
curl http://localhost:8080/api/products

# Search products
curl "http://localhost:8080/api/products?search=leather"

# Filter by category
curl "http://localhost:8080/api/products?category=electronics"

# Create new product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Product",
    "description": "Product description",
    "price": 99.99,
    "category": "electronics",
    "imageUrl": "https://example.com/image.jpg",
    "rating": 4.5,
    "inStock": true
  }'
```

## Project Structure

```
├── backend/
│   ├── src/main/java/com/catalog/
│   │   ├── CatalogApplication.java
│   │   ├── controller/
│   │   │   └── ProductController.java
│   │   ├── entity/
│   │   │   └── Product.java
│   │   ├── repository/
│   │   │   └── ProductRepository.java
│   │   ├── service/
│   │   │   └── ProductService.java
│   │   └── config/
│   │       └── WebConfig.java
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── data.sql
│   └── pom.xml
├── src/
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
├── package.json
└── README.md
```

## MySQL Configuration

### Connection Settings
The application connects to MySQL using these default settings:
- **Host:** localhost
- **Port:** 3306
- **Database:** catalog_db (created automatically)
- **Username:** root
- **Password:** password

### Customizing Database Connection
Update `backend/src/main/resources/application.properties`:

```properties
# For different host/port
spring.datasource.url=jdbc:mysql://your-host:3306/catalog_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC

# For different credentials
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Database Initialization
- Tables are created automatically using JPA annotations
- Sample data is inserted from `data.sql` on first run
- Use `spring.jpa.hibernate.ddl-auto=create` to recreate tables (will delete existing data)

## Customization

### Adding New Product Fields
1. Update the `Product` entity in `backend/src/main/java/com/catalog/entity/Product.java`
2. Update the React `Product` interface in `src/App.tsx`
3. Update the frontend form components as needed

### Styling
The application uses Tailwind CSS for styling. Customize the design by:
- Modifying Tailwind classes in React components
- Updating the color scheme in `src/App.tsx`
- Adding custom CSS in `src/index.css`

### Database Configuration
- Update `application.properties` for different database configurations
- Modify `data.sql` to change sample data
- Use different `spring.jpa.hibernate.ddl-auto` values for different environments

## Deployment

### Production Deployment
1. **Ensure MySQL is running** on your production server

2. **Build the application:**
```bash
cd backend
mvn clean package -Pproduction
```

3. **Deploy the generated JAR:**
```bash
java -jar target/catalog-backend-1.0.0.jar
```

### Docker Deployment
Create a `Dockerfile` in the backend directory:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/catalog-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Use Docker Compose with MySQL:
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
  
  app:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/catalog_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
```

## Troubleshooting

### Common Issues

1. **MySQL Connection Failed**
   - Ensure MySQL service is running
   - Check username/password in `application.properties`
   - Verify MySQL is accessible on localhost:3306

2. **Port Already in Use**
   - Change server port in `application.properties`: `server.port=8081`
   - Or stop the process using the port

3. **Frontend Build Fails**
   - Ensure Node.js 18+ is installed
   - Run `npm install` in the root directory
   - Check for any TypeScript errors

## Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License
This project is licensed under the MIT License.