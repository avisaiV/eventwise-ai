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

java -version
mvn -v
docker --version
ollama --version

Clone & Build
git clone https://github.com/avisaiV/eventwise-ai.git
cd eventwise-ai
mvn -DskipTests clean package

Start Infrastructure (Kafka / Redpanda)

From the project root:

docker compose up -d


Check running containers:

docker ps

Start All Microservices
Terminal A – event-service (8083)
cd event-service
mvn -q -DskipTests spring-boot:run

Terminal B – booking-service (8081)
cd booking-service
mvn -q -DskipTests spring-boot:run

Terminal C – recommendation-service (8084)

Set env vars in this terminal before starting:

$env:EXPLAINER_MODE = 'llm'
$env:OLLAMA_BASE_URL = 'http://localhost:11434'
$env:OLLAMA_MODEL = 'llama3.2'


Then:

cd recommendation-service
mvn -q -DskipTests spring-boot:run

LLM Setup (Ollama)
ollama serve
ollama pull llama3.2

Quick End-to-End Demo (PowerShell)
A) Create Events
$e1 = Invoke-RestMethod -Method Post http://localhost:8083/events `
  -ContentType application/json `
  -Body (@{ title="UOW Tech Fest"; category="Tech"; capacity=100 } | ConvertTo-Json)

$e2 = Invoke-RestMethod -Method Post http://localhost:8083/events `
  -ContentType application/json `
  -Body (@{ title="AI Summit"; category="Tech"; capacity=5 } | ConvertTo-Json)

B) Create Bookings (capacity guard)
Invoke-RestMethod -Method Post http://localhost:8081/bookings `
  -ContentType application/json `
  -Body (@{ userId=1; eventId=$e1.id; qty=3 } | ConvertTo-Json)

Invoke-RestMethod -Method Post http://localhost:8081/bookings `
  -ContentType application/json `
  -Body (@{ userId=2; eventId=$e2.id; qty=4 } | ConvertTo-Json)


Exceeds capacity → expect HTTP 409:

try {
  Invoke-RestMethod -Method Post http://localhost:8081/bookings `
    -ContentType application/json `
    -Body (@{ userId=3; eventId=$e2.id; qty=2 } | ConvertTo-Json)
} catch {
  $_.Exception.Response.StatusCode.value__
}

C) Recommendations & Explanations
Invoke-RestMethod "http://localhost:8084/recommendations?limit=5" | ConvertTo-Json
Invoke-RestMethod "http://localhost:8084/explanations?eventId=$($e1.id)" | ConvertTo-Json

D) CRUD Checks
Invoke-RestMethod "http://localhost:8081/bookings/1" | ConvertTo-Json
Invoke-RestMethod "http://localhost:8083/events/$($e1.id)" | ConvertTo-Json

Stopping Everything
docker compose down


Stop each microservice with Ctrl + C.

Troubleshooting (Fast)

Docker won’t start / virtualization issues
Enable CPU virtualization, turn on Virtual Machine Platform + Windows Hypervisor Platform, install WSL2, reboot.

LLM not responding
Check the model:

curl http://localhost:11434/api/tags


Empty recommendations
Create bookings first — the Kafka Streams state store only updates from booking events.

Built and maintained by Avisai Vunisasaro.
