# springboot-task — REST API + SSE

Spring Boot 3.4.5 application demonstrating a Task management REST API with real-time Server-Sent Events (SSE) notifications.

**Version**: 1.0.0 | **Java**: 21 | **Group**: org.lambdasys

## Features

- CRUD REST API for Tasks (`/api/v1/tasks`)
- Real-time SSE stream for task events (`/api/v1/tasks/events`)
- In-memory H2 database
- Spring Boot Actuator health endpoint

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/tasks` | List all tasks |
| POST | `/api/v1/tasks` | Create a task |
| GET | `/api/v1/tasks/{id}` | Get task by ID |
| PUT | `/api/v1/tasks/{id}` | Update a task |
| DELETE | `/api/v1/tasks/{id}` | Delete a task |
| GET | `/api/v1/tasks/events` | Subscribe to SSE stream |

## Task Model

```json
{
  "title": "My task",
  "description": "Optional description",
  "status": "TODO | IN_PROGRESS | DONE"
}
```

## Running Locally

**With Maven:**
```shell
./mvnw spring-boot:run
```

**With Docker Compose:**
```shell
docker-compose up
```

App will be available at `http://localhost:8080`.

## SSE Demo

Open the event stream in one terminal:
```shell
curl -N http://localhost:8080/api/v1/tasks/events
```

In another terminal, create a task:
```shell
curl -X POST http://localhost:8080/api/v1/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Hello SSE","status":"TODO"}'
```

The first terminal will receive:
```
event:task-created
data:{"id":1,"title":"Hello SSE","status":"TODO",...}
```

## Health Check

```shell
curl http://localhost:8080/actuator/health
```

## Running Tests

```shell
./mvnw test
```

## Project Structure

```
src/main/java/org/lambdasys/
├── Application.java
├── controller/
│   ├── TaskController.java
│   └── TaskSseController.java
├── service/
│   └── TaskService.java
├── repository/
│   └── TaskRepository.java
├── model/
│   ├── Task.java
│   └── TaskStatus.java
├── dto/
│   ├── TaskRequest.java
│   └── TaskResponse.java
└── exception/
    └── GlobalExceptionHandler.java
```
