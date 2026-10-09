# Invoice Processor

Spring Boot 3.5.16 microservice that consumes JSON invoices from Kafka, validates them, transforms valid invoices to XML, and publishes the XML to Kafka.

## Deliverables

- `README.md` with setup instructions
- `samples/incoming-invoice.json` with a sample Kafka message
- This README's **Technical Decisions** section

## Requirements

- Java 27, as configured in `pom.xml`
- Docker with Docker Compose
- Maven is not required; use the included `./mvnw` wrapper
- H2 is included as a runtime dependency and requires no separate installation

## Setup

### Start Kafka

Kafka runs locally in single-node mode. The Compose initialization service creates both application topics.

```bash
docker compose up -d
docker compose ps
```

The `kafka-init` container is expected to exit successfully after creating the topics.

### Start the application

The application creates a persistent file-backed H2 database in `./data` on first startup.

```bash
./mvnw spring-boot:run
```

The default endpoints and connections are:

- Kafka: `localhost:9092`
- H2 database: `./data/invoice_processor`
- H2 username: `sa`
- H2 password: empty
- Application: `http://localhost:8080`

If port `8080` is already in use:

```bash
SERVER_PORT=8081 ./mvnw spring-boot:run
```

### Configuration

The following environment variables can override the defaults:

| Variable | Default | Purpose |
| --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `KAFKA_CONSUMER_GROUP` | `invoice-processor` | Consumer group name |
| `DB_URL` | `jdbc:h2:file:./data/invoice_processor;DB_CLOSE_ON_EXIT=FALSE` | H2 JDBC URL |
| `DB_USERNAME` | `sa` | H2 username |
| `DB_PASSWORD` | empty | H2 password |

Example:

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_CONSUMER_GROUP=invoice-processor
export DB_URL='jdbc:h2:file:./data/invoice_processor;DB_CLOSE_ON_EXIT=FALSE'
```

## Processing Flow

1. Consume a JSON string from `incoming-invoices`.
2. Deserialize and validate the invoice structure, ISO country, country-specific customer ID, and line amounts.
3. Store invalid payloads and validation reasons in the `invalid_invoices` H2 table.
4. Transform valid invoices to XML and calculate the total including VAT.
5. Publish the XML to `validated-invoices`.

Invalid business messages are persisted and acknowledged so they are not retried indefinitely. Kafka and other infrastructure failures are retried twice with a one-second backoff.

## Kafka Topics

- `incoming-invoices`: input JSON invoice messages
- `validated-invoices`: output XML invoice messages

## Sample Kafka Message

The sample message is stored in [`samples/incoming-invoice.json`](samples/incoming-invoice.json).

Send it to Kafka:

```bash
docker exec -i invoice-kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic incoming-invoices < samples/incoming-invoice.json
```

Read the transformed output:

```bash
docker exec invoice-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic validated-invoices \
  --from-beginning \
  --timeout-ms 10000
```

The sample produces an XML document containing a total of `1190.00`:

```xml
<Invoice>
  <Id>INV-20240101-001</Id>
  <Country>DE</Country>
  <Total>1190.00</Total>
</Invoice>
```

## REST API

The service exposes a REST endpoint to inspect the invalid invoices stored in H2. The contract is defined API-first in [`src/main/resources/openapi/invalid-invoices-api.yaml`](src/main/resources/openapi/invalid-invoices-api.yaml).

### List invalid invoices

```http
GET /api/v1/invalid-invoices?page=0&size=20&sort=receivedAt,desc
```

| Parameter | Default | Description |
| --- | --- | --- |
| `page` | `0` | Zero-based page index |
| `size` | `20` | Page size (1–100) |
| `sort` | `receivedAt,desc` | `property,asc\|desc`; repeatable. Allowed properties: `id`, `invoiceId`, `receivedAt` |

Example:

```bash
curl "http://localhost:8080/api/v1/invalid-invoices?page=0&size=2&sort=receivedAt,desc"
```

```json
{
  "content": [
    {
      "id": 3,
      "invoiceId": "INV-20240101-003",
      "reason": "amount must be greater than zero",
      "receivedAt": "2024-01-03T10:00:00Z"
    }
  ],
  "number": 0,
  "size": 2,
  "numberOfElements": 1,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "empty": false
}
```

The raw invalid payload is not included in the list response because it is stored as a large LONGTEXT column.

## Technical Decisions

### Native Spring Kafka

The implementation uses `@KafkaListener` and `KafkaTemplate` directly. Spring Cloud Stream and Kafka Streams are not used because the exercise requires a straightforward consumer/producer pipeline and native Spring Kafka keeps the processing path explicit.

### Raw JSON consumption

The listener consumes the Kafka value as a raw `String` and deserializes it with Jackson inside the application. This allows malformed JSON to be stored as an invalid invoice instead of being rejected before the listener can persist the original payload.

### Validation

Bean Validation handles structural rules. A dedicated validation service handles ISO country membership and country-specific customer ID formats. `BigDecimal` is used for monetary values to avoid floating-point rounding errors.

### H2 persistence

Invalid invoices are stored in a file-backed H2 database for local development. Tests use in-memory H2 databases so they remain isolated and require no external database service. Hibernate schema updates are enabled for the scope of this exercise.

### XML transformation

Jackson XML produces a deliberately small XML contract containing `Id`, `Country`, and VAT-inclusive `Total`. The total is calculated as `amount * (1 + vatRate)` for each line and rounded to two decimal places.

### API-First REST endpoint

The invalid invoice list endpoint is defined API-first in `src/main/resources/openapi/invalid-invoices-api.yaml`. The `openapi-generator-maven-plugin` (Spring generator, `interfaceOnly`) generates the `InvalidInvoicesApi` interface and the response DTOs into `target/generated-sources/openapi` at build time, and `InvalidInvoiceController` implements the generated interface. The spec uses the `x-spring-paginated` extension so the generated method receives a Spring `Pageable`; the service returns a Spring `Page<InvalidInvoice>` that is mapped to the generated `InvalidInvoicePage` DTO. Spring Data JPA and springdoc already provide `Pageable` resolution and documentation, so no additional runtime dependency is required.

### Error handling

Business validation and JSON errors are persisted and acknowledged. Publishing, transformation, and infrastructure failures are allowed to propagate to Spring Kafka's `DefaultErrorHandler`, which retries twice before recovery.

### Local infrastructure

Docker Compose provides a single Kafka broker in KRaft mode and creates the required topics. H2 is embedded in the application, so no database container is required.

## Tests

Run the complete test suite:

```bash
./mvnw test
```

The suite includes validation and XML unit tests, an H2 repository test, an application context test, a MockMvc pagination test for the REST endpoint, and an embedded Kafka end-to-end test. It does not require Docker, the external Kafka broker, or a separate database.

## Developer Consoles

- H2 console: `http://localhost:8080/h2-console`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

> The Kafka processing path does not require REST, but the invalid invoice list endpoint is documented in Swagger UI.

## Repository Hygiene

Generated H2 files under `data/`, Maven output under `target/`, and IDE metadata are ignored and should not be committed.
