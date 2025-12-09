# EventWise

> EventWise is a lightweight event-booking platform showcasing modern backend engineering: microservices, event-driven messaging, real-time streaming, and LLM-powered explanations — all running locally on Windows.

---

## Features

- **CRUD Microservices**
  - `event-service` — event creation, categories, capacity  
  - `booking-service` — booking validation + emits events  
  - `recommendation-service` — Kafka Streams, popularity scoring, LLM explanations  

- **Event-driven messaging** via Kafka (Redpanda)

- **Real-time aggregation** using Spring Cloud Stream + Kafka Streams

- **Human-readable explanations**
  - Rule-based engine  
  - Or local LLM via **Ollama** (private & free)

---

## Running the Project (Windows)

Make sure you have **Java 21, Maven, Docker Desktop, and Ollama** installed.

### Install dependencies (winget)

```bash
winget install -e --id EclipseAdoptium.Temurin.21.JDK
winget install -e --id Apache.Maven
winget install -e --id Docker.DockerDesktop
winget install -e --id Ollama.Ollama
winget install -e --id Postman.Postman   # optional
```

### Verify installs

```bash
java -version
mvn -v
docker --version
ollama --version
```

---

## Clone & Build

```bash
git clone https://github.com/avisaiV/eventwise-ai.git
cd eventwise-ai
mvn -DskipTests clean package
```

---

## Start Infrastructure (Kafka / Redpanda)

```bash
docker compose up -d
```

Check containers:

```bash
docker ps
```

---

## Run the Microservices

### event-service (8083)

```bash
cd event-service
mvn -q -DskipTests spring-boot:run
```

### booking-service (8081)

```bash
cd booking-service
mvn -q -DskipTests spring-boot:run
```

### recommendation-service (8084)

Set LLM mode:

```powershell
$env:EXPLAINER_MODE = 'llm'
$env:OLLAMA_BASE_URL = 'http://localhost:11434'
$env:OLLAMA_MODEL = 'llama3.2'
```

Run service:

```bash
cd recommendation-service
mvn -q -DskipTests spring-boot:run
```

---

## LLM Setup (Ollama)

```bash
ollama serve
ollama pull llama3.2
```

---

## Quick End-to-End Demo

### Create Events

```powershell
$e1 = Invoke-RestMethod -Method Post http://localhost:8083/events `
  -ContentType application/json `
  -Body (@{ title="UOW Tech Fest"; category="Tech"; capacity=100 } | ConvertTo-Json)

$e2 = Invoke-RestMethod -Method Post http://localhost:8083/events `
  -ContentType application/json `
  -Body (@{ title="AI Summit"; category="Tech"; capacity=5 } | ConvertTo-Json)
```

### Create Bookings

```powershell
Invoke-RestMethod -Method Post http://localhost:8081/bookings `
  -ContentType application/json `
  -Body (@{ userId=1; eventId=$e1.id; qty=3 } | ConvertTo-Json)
```

Capacity guard:

```powershell
try {
  Invoke-RestMethod -Method Post http://localhost:8081/bookings `
    -ContentType application/json `
    -Body (@{ userId=3; eventId=$e2.id; qty=2 } | ConvertTo-Json)
} catch {
  $_.Exception.Response.StatusCode.value__
}
```

---

## Recommendations (Kafka Streams)

```powershell
Invoke-RestMethod "http://localhost:8084/recommendations?limit=5" | ConvertTo-Json
```

## Explanation (LLM or Rule-based)

```powershell
Invoke-RestMethod "http://localhost:8084/explanations?eventId=$($e1.id)" | ConvertTo-Json
```

---

## Basic CRUD

```powershell
Invoke-RestMethod "http://localhost:8081/bookings/1" | ConvertTo-Json
Invoke-RestMethod "http://localhost:8083/events/$($e1.id)" | ConvertTo-Json
```

---

## Shutdown

Stop each service (Ctrl + C)

Then stop Kafka:

```bash
docker compose down
```

---

## Troubleshooting

- Docker failing → enable Virtualization + WSL2  
- Ollama port error → already running  
- No recommendations → bookings must be created  
- LLM slow → model not installed  

---

Built by **Avisai**  
