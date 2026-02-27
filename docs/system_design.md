# Backend Current Status: Tutorial Platform

## Architecture Overview
The backend is a **Java Spring Boot** application using **JPA/Hibernate** for persistence. It follows a standard Layered Architecture: **Controller -> Service -> Repository -> Entity**.

## Data Model (ER Relationship)

```mermaid
erDiagram
    MEMBER ||--o{ LEARNING_RECORD : has
    MEMBER ||--o{ GYM_CHALLENGE_RECORD : attempts
    MEMBER ||--o{ VISITOR_LOG : "tracked by"
    JOURNEY ||--o{ CHAPTER : contains
    JOURNEY ||--o{ MISSION : includes
    JOURNEY ||--o{ SKILL : defines
    JOURNEY ||--o{ GYM : hosts
    CHAPTER ||--o{ GYM : contains
    GYM ||--o{ CHALLENGE : offers
    GYM ||--o{ GYM_SUBMISSION : receives
    GYM ||--o{ LESSON : relates_to
    GYM_CHALLENGE_RECORD }|--|| MEMBER : "links to"
    GYM_CHALLENGE_RECORD }|--|| GYM : "links to"
```

### Key Entities
- **Member**: Stores user profile, `exp`, `coin`, `level`, and `jobTitle`. Added fields for `githubUrl` and `discordId`.
- **VisitorLog**: Tracks guest/visitor activity prior to registration.
- **Mission**: Defines specific goals within a Journey, evaluated by `ConditionEvaluator`.

## API Endpoints
- **Auth**: `/api/auth/register`, `/api/auth/quick-register`, `/api/auth/logout`.
- **GymChallengeRecordController**: `GET /api/users/{userId}/journeys/gyms/challenges/records`.
- **MissionController**: `POST /api/missions/{id}/accept`.
- **MemberController**: 
    - `GET /api/users/{userId}`: User profile.
    - `PATCH /api/users/{userId}`: Update profile (e.g., jobTitle/role).
    - `GET /api/leaderboard`: Leaderboard data.

## Tech Stack
- **Language**: Java
- **Framework**: Spring Boot, Spring Data JPA
- **Database**: PostgreSQL (implied by `jsonb` usage)
- **Utilities**: Lombok, Jackson
