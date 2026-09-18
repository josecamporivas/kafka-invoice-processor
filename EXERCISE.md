# 🧪 Technical Test – Java Spring Boot Developer

**Estimated time:** Up to 3 hours

## 📘 Context

At Marosa, we process thousands of electronic invoices daily. These invoices arrive via Kafka topics, are validated, transformed, and routed to multiple destinations such as tax authority APIs or internal storage systems.

## 🎯 The Challenge

Build a Spring Boot microservice to process electronic invoices via Kafka queues.

### 1️⃣ Kafka Consumer

Consume JSON messages from a Kafka topic named `incoming-invoices` with the following format:

```
{
  "invoiceId": "INV-20240101-001",
  "customerId": "12345678A",
  "country": "DE",
  "issueDate": "2024-01-01",
  "lines": [
    { "description": "Laptop", "amount": 1000, "vatRate": 0.19 }
  ]
}
```

### 2️⃣ Validation

- Ensure `country` is a valid ISO code (e.g., "DE", "ES", "FR")
- Validate `customerId` format depending on the country
- No line item can have `amount <= 0`

### 3️⃣ Transformation

Transform the invoice into a simple XML format (you can create your own simplified version of UBL/FacturaE):

```
<Invoice>
  <Id>INV-20240101-001</Id>
  <Country>DE</Country>
  <Total>1190.0</Total>
</Invoice>
    
```

### 4️⃣ Kafka Producer

Publish the resulting XML to a second Kafka topic: `validated-invoices`.

### 💡 Bonus (Optional)

- Store invalid invoices in an H2 database
- Use Spring Cloud Stream or Kafka Streams
- Document with Swagger/OpenAPI
- Add unit and integration tests

## 📦 Deliverables

- `README.md` with setup instructions
- Sample Kafka message for testing
- Explanation of technical decisions made

## ⚙️ Requirements

- Spring Boot 3.x
- Kafka
- Maven or Gradle
- Docker Compose (optional, for Kafka broker)

## ✅ Evaluation Criteria

- Architecture and code design
- Proper use of Kafka and event-driven patterns
- Validation and error handling
- Code quality and readability
- Tests and documentation

We look forward to seeing your solution! ✨