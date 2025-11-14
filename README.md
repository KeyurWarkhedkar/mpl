# Math Premier League Portal

A multi-team quiz platform with concurrent question assignment, real-time leaderboard updates, and race condition prevention at scale.

## Problem Statement

When multiple teams simultaneously request questions in a competitive quiz environment, the system must ensure:

- Each team gets exactly one unique question
- No two teams receive the same question
- No partial states (team without question assignment)
- System remains responsive under concurrent load

## Technical Solution

### Core Features

#### 1. Thread-Safe Question Assignment

- Pessimistic locking (`SELECT FOR UPDATE`) prevents race conditions
- Database-level unique constraints as safety net
- Transactional integrity across team creation and question assignment

#### 2. Real-Time Leaderboard Updates

- WebSocket (STOMP) for live score broadcasting
- Server pushes updates to all connected clients when team submits answer
- No polling required - instant UI updates across all devices

#### 3. Concurrent Request Handling

- Handles 280+ simultaneous team registrations
- Zero duplicate question assignments under load
- Graceful handling of resource exhaustion

## Architecture

```
┌─────────────┐
│   Client    │ ──HTTP POST──> Registration/Answer Submission
│  (Browser)  │ <──WebSocket─> Live Score Updates
└─────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│     Spring Boot Application         │
│  ┌──────────────────────────────┐   │
│  │  Question Service            │   │
│  │  - Pessimistic locking       │   │
│  │  - Transactional integrity   │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │  Mystery Service             │   │
│  │  - Points deduction          │   │
│  │  - Difficulty-based alloc    │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │  WebSocket Controller        │   │
│  │  - STOMP messaging           │   │
│  │  - Score broadcast           │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
       │
       ▼
┌─────────────┐
│    MySQL    │ (Row-level locking)
└─────────────┘
```

## Concurrency Handling

### Race Condition Prevention

```java
@Transactional
public Question getQuestion(QuestionDto questionDto) {
    // 1. Lock question row to prevent concurrent access
    Question question = questionDao.findByIdWithLock(questionDto.getQuestionId());
    
    // 2. Check if team already exists
    Optional<Teams> existingTeam = teamDao.findByTeamName(teamName);
    if (existingTeam.isPresent()) {
        return getExistingAssignment(existingTeam.get());
    }
    
    // 3. Atomic team creation + question assignment
    return createTeamAndAssignQuestion(teamName, question);
}
```

### Mystery Question Assignment with Points Deduction

```java
@Transactional
public MysteryQuestion assignMysteryQuestion(MysteryBoxDto mysteryBoxDto) {
    // 1. Lock team row to prevent concurrent points deduction
    Teams team = teamDao.findByTeamNameWithLock(mysteryBoxDto.getTeamName());
    
    // 2. Check if team already has mystery question
    if (team.getMysteryQuestion() != null) {
        return team.getMysteryQuestion(); // Idempotent response
    }
    
    // 3. Lock and allocate available mystery question
    List<MysteryQuestion> availableQuestions = 
        mysteryQuestionDao.findByDifficultyAndQuestionStatus(difficulty, UNALLOCATED);
    
    // 4. Atomic points deduction + question assignment
    team.setPoints(team.getPoints() - mysteryBoxDto.getPointsDeducted());
    team.setMysteryQuestion(selectedQuestion);
    teamDao.save(team);
}
```

### Database Constraints

Database constraints ensure data integrity:

- Unique constraint on `team_name`
- Unique constraint on `question_id` in team_question mapping
- Unique constraint on `mystery_question` per team
- Foreign key relationships maintain referential integrity

## Load Testing Results

### Team Registration & Question Assignment

**Test Configuration:**

- **Tool:** K6 (Grafana)
- **Concurrent Virtual Users:** 280
- **Test Scenarios:**
  - High contention (50 teams competing for 2 questions)
  - Normal load (280 teams ramping over 30 seconds)
- **Total Request Attempts:** 330+

**Performance Metrics:**

| Metric | Value |
|--------|-------|
| Successful Assignments | 250 |
| Duplicate Assignments | 0 |
| Average Response Time | ~100ms |
| P95 Response Time | 119.98ms |
| P99 Response Time | 175.99ms |
| Data Integrity | 100% |

**Database Integrity Verification:**

```sql
-- Post-test validation queries
SELECT COUNT(*) FROM teams;                         -- Result: 250
SELECT COUNT(*) FROM team_question;                 -- Result: 250
SELECT question_id, COUNT(*) FROM team_question 
GROUP BY question_id HAVING COUNT(*) > 1;        -- Result: Empty (0 duplicates)
```

**Key Achievement:** Under extreme concurrent load (330 teams competing for 250 limited questions), the system correctly assigned all 250 questions with **zero race conditions**, **zero duplicate assignments**, and maintained **sub-120ms P95 response times**.

### Mystery Box System Performance

**Test Configuration:**

- **Tool:** K6 (Grafana)
- **Concurrent Virtual Users:** 50
- **Test Scenario:** All 250 registered teams requesting mystery questions simultaneously
- **Points Deduction:** 100 points per mystery question

**Performance Metrics:**

| Metric | Value |
|--------|-------|
| Successful Assignments | 250 |
| Duplicate Attempts | 0 |
| Average Response Time | ~150ms |
| P95 Response Time | 474.51ms |
| Error Rate | 0% |
| Points Deduction Accuracy | 100% |

**Database Integrity Verification:**

```sql
-- Post-test validation queries
SELECT COUNT(*) FROM teams WHERE mystery_question IS NOT NULL;  -- Result: 250

SELECT mystery_question, COUNT(*) FROM teams 
WHERE mystery_question IS NOT NULL 
GROUP BY mystery_question HAVING COUNT(*) > 1;  -- Result: Empty (0 duplicates)

-- Points verification
SELECT AVG(points), MIN(points), MAX(points) FROM teams 
WHERE mystery_question IS NOT NULL;  -- Result: 900, 900, 900 (100 points deducted from 1000)
```

### Key Achievements

- Under extreme concurrent load (330 teams competing for 250 limited questions), the system correctly assigned all 250 questions with **zero race conditions**, **zero duplicate assignments**, and maintained **sub-120ms P95 response times**
- Mystery box system handled 250 concurrent requests with **perfect points deduction accuracy** and **zero financial inconsistencies**
- Both systems maintained **100% data integrity** across high-concurrency scenarios

## Tech Stack

- **Backend:** Spring Boot 3.x
- **Real-time Communication:** Spring WebSocket (STOMP)
- **Database:** MySQL 8.0+ with InnoDB engine
- **ORM:** Spring Data JPA with Hibernate
- **Load Testing:** K6 by Grafana
- **Unit Testing:** JUnit 5, Mockito
- **Build Tool:** Maven

## Key Technical Decisions

### 1. Pessimistic Locking vs Optimistic Locking

**Decision:** Pessimistic locking (`SELECT FOR UPDATE`)

**Rationale:**
- High contention scenario (limited questions, many teams)
- Optimistic locking would cause excessive retry storms
- Database-level locks ensure correctness across all application instances

### 2. WebSocket vs Server-Sent Events (SSE)

**Decision:** WebSocket with STOMP protocol

**Rationale:**
- Bidirectional communication required (teams submit answers + receive leaderboard updates)
- Lower latency for competitive gaming scenario
- STOMP provides message routing and pub/sub patterns

### 3. Database Constraints as Safety Net

**Decision:** Application-level locking + database-level constraints

**Rationale:**
- Defense in depth strategy
- Constraints catch edge cases (network partitions, transaction anomalies)
- Prevents data corruption even if application logic has bugs

### 4. Points Deduction Strategy

**Decision:** Atomic database transaction with row-level locking

**Rationale:**
- Prevents overspending in concurrent scenarios
- Ensures financial consistency across all operations
- Database-level atomicity guarantees no partial updates

## Running Locally

### Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Setup

1. **Clone repository**

```bash
git clone [your-repo-url]
cd quiz-platform
```

2. **Configure database**

```bash
# Create database
mysql -u root -p
CREATE DATABASE quiz_db;
```

3. **Update application.properties**

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/quiz_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. **Run application**

```bash
mvn spring-boot:run
```

5. **Run load tests**

```bash
k6 run k6-load-test.js
k6 run k6-mystery-test.js
```

## API Endpoints

### Question Assignment

```
POST /question/get
Content-Type: application/json

Request Body:
{
  "teamName": "string",
  "questionId": "string"
}

Response: Question object
```

### Mystery Box Assignment

```
POST /mystery-box/assign  
Content-Type: application/json

Request Body:
{
  "teamName": "string",
  "difficulty": "EASY|MEDIUM|HARD",
  "pointsDeducted": number
}

Response: MysteryQuestion object
```

### WebSocket Connection

```
WS /ws
Protocol: STOMP

Subscribe to: /topic/leaderboard
Send to: /app/answer
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/quiz/
│   │       ├── controller/
│   │       ├── service/
│   │       │   ├── QuestionService.java
│   │       │   ├── MysteryService.java
│   │       │   └── LeaderboardService.java
│   │       ├── dao/
│   │       ├── model/
│   │       └── config/
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        ├── k6/
        │   ├── load-test.js
        │   └── mystery-test.js
        └── unit/
```

## Future Enhancements

- Redis distributed locking for multi-instance deployment
- Database read replicas for leaderboard queries
- WebSocket horizontal scaling with Redis pub/sub
- Question difficulty-based dynamic scoring
- Admin dashboard for real-time monitoring
- Automated rollback on constraint violations
- Circuit breaker pattern for external service calls

## Testing

**Unit Tests:**

```bash
mvn test
```

**Integration Tests:**

```bash
mvn verify
```

**Load Tests:**

```bash
k6 run k6-load-test.js
k6 run k6-mystery-test.js
```

## License

MIT

---

Developed to demonstrate concurrent system design, real-time communication patterns, production-grade race condition handling, and financial transaction integrity under high load.
