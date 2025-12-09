EventWise – Event Platform Microservices Demo

EventWise is a lightweight event-booking platform showcasing modern backend engineering:
microservices, event-driven messaging, real-time streaming, and LLM-powered explanations.

What EventWise Includes

CRUD microservices

event-service (events, capacity)

booking-service (bookings, validation, emits events)

recommendation-service (Kafka Streams, popularity scoring, LLM explanations)

Event-driven messaging via Kafka (Redpanda)

Real-time aggregation using Spring Cloud Stream + Kafka Streams

Human-readable explanations using either:

Rule-based engine, or

Local LLM through Ollama (free, private)

Full Windows setup guide for quick testing

Running the Project (Windows)

Make sure you have Java 21, Maven, Docker Desktop, and Ollama installed.

Verify installs
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
Terminal A — event-service (8083)
cd event-service
mvn -q -DskipTests spring-boot:run

Terminal B — booking-service (8081)
cd booking-service
mvn -q -DskipTests spring-boot:run

Terminal C — recommendation-service (8084)

Before running, set environment variables for LLM mode:

$env:EXPLAINER_MODE = 'llm'
$env:OLLAMA_BASE_URL = 'http://localhost:11434'
$env:OLLAMA_MODEL = 'llama3.2'


Then run:

cd recommendation-service
mvn -q -DskipTests spring-boot:run

LLM Setup (Ollama)

Start Ollama server:

ollama serve


Pull the lightweight model:

ollama pull llama3.2

Quick End-to-End Demo
A) Create Events
$e1 = Invoke-RestMethod -Method Post http://localhost:8083/events `
  -ContentType application/json `
  -Body (@{ title="UOW Tech Fest"; category="Tech"; capacity=100 } | ConvertTo-Json)

$e2 = Invoke-RestMethod -Method Post http://localhost:8083/events `
  -ContentType application/json `
  -Body (@{ title="AI Summit"; category="Tech"; capacity=5 } | ConvertTo-Json)

B) Create Bookings
Invoke-RestMethod -Method Post http://localhost:8081/bookings `
  -ContentType application/json `
  -Body (@{ userId=1; eventId=$e1.id; qty=3 } | ConvertTo-Json)


Capacity guard (expected HTTP 409):

try { 
  Invoke-RestMethod -Method Post http://localhost:8081/bookings `
  -ContentType application/json `
  -Body (@{ userId=3; eventId=$e2.id; qty=2 } | ConvertTo-Json)
} catch { $_.Exception.Response.StatusCode.value__ }

C) Recommendations & Explanations

Top-N events:

Invoke-RestMethod "http://localhost:8084/recommendations?limit=5" | ConvertTo-Json


Human-readable explanation:

Invoke-RestMethod "http://localhost:8084/explanations?eventId=$($e1.id)" | ConvertTo-Json

D) CRUD Checks
Invoke-RestMethod "http://localhost:8081/bookings/1" | ConvertTo-Json
Invoke-RestMethod "http://localhost:8083/events/$($e1.id)" | ConvertTo-Json

Stopping Everything
docker compose down


Stop all microservices with CTRL+C.

Troubleshooting
Docker not starting

Enable CPU virtualization

Turn on Virtual Machine Platform + Windows Hypervisor Platform

Install WSL2

Reboot

LLM not responding

Check model:

curl http://localhost:11434/api/tags

No recommendations

You must create bookings first — the Kafka Streams state store only updates on booking events.
