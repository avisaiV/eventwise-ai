# EventWise – Event Platform Microservices Demo

> EventWise is a lightweight event-booking platform showcasing modern backend engineering: microservices, event-driven messaging, real-time streaming, and LLM-powered explanations.

---

## What EventWise Includes

- CRUD microservices  
  - `event-service` (events, capacity)  
  - `booking-service` (bookings, validation, emits events)  
  - `recommendation-service` (Kafka Streams, popularity scoring, LLM explanations)
- Event-driven messaging via **Kafka (Redpanda)**
- Real-time aggregation using **Spring Cloud Stream + Kafka Streams**
- Human-readable explanations using:
  - Rule-based engine, or  
  - Local LLM through **Ollama** (free, private)
- Full Windows setup guide for quick testing

---

## Running the Project (Windows)

Make sure you have **Java 21**, **Maven**, **Docker Desktop**, and **Ollama** installed.

### Verify installs

```bash
java -version
mvn -v
docker --version
ollama --version
