# Calculator API 

**This project is part of the WIT Software Java Developer Challenge.**  
It is a RESTful Calculator API built with **Spring Boot & Apache Kafka**.

## Features
✅ Supports **Addition, Subtraction, Multiplication, Division**  
✅ Handles **arbitrary precision numbers (BigDecimal)**  
✅ Uses **Apache Kafka** for async communication  
✅ Docker support with **Docker Compose**  
✅ **SLF4J + Logback** for structured logging  
✅ Unit tests with **JUnit & Mockito**

---

## 📌 Setup & Installation

### **🔹 1. Clone the repository**

git clone https://github.com/yuran-eduardo/calculator-api.git cd calculator-api

### **🔹 2. Build the project**

For **Maven**: mvn clean install

For **Gradle**: ./gradlew build 


### **🔹 3. Running with Docker**
Ensure **Docker** is installed. Then run:

docker-compose up -d

This starts:
✅ `rest-service` (API)  
✅ `calculator-service` (Computations)  
✅ `Kafka` (for messaging)

---

## 📌 API Usage
The API is available at:

http://localhost:8081/api/calculate


### **Supported Operations**
| Operation | URL Example |
|-----------|------------|
| Addition | `GET /api/calculate?operation=sum&a=5&b=3` → `{ "result": 8 }` |
| Subtraction | `GET /api/calculate?operation=sub&a=10&b=4` → `{ "result": 6 }` |
| Multiplication | `GET /api/calculate?operation=mul&a=6&b=7` → `{ "result": 42 }` |
| Division | `GET /api/calculate?operation=div&a=10&b=2` → `{ "result": 5.0000000000 }` |
| **Division by Zero** | `GET /api/calculate?operation=div&a=5&b=0` → `{ "error": "Division by zero is not allowed" }` |
| **Invalid Operation** | `GET /api/calculate?operation=mod&a=5&b=2` → `{ "error": "Unsupported operation" }` |

---

## 📌 Running Tests
Run **unit tests**:

mvn test

or

./gradlew test


---

## 📌 Technologies Used
- **Java 17**
- **Spring Boot 3**
- **Apache Kafka**
- **JUnit & Mockito**
- **Docker & Docker Compose**
- **SLF4J + Logback**


---

## 📌 Author
**Yuran Eduardo Mangueze**  
📧 Email: yuranmangueze@gmail.com 
🔗 GitHub: [yuran-eduardo](https://github.com/yuran-eduardo)
