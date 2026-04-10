# 과제 A — 수강 신청 시스템

## 프로젝트 개요

라이브 클래스 플랫폼의 수강 신청 시스템입니다.
크리에이터(강사)가 강의를 개설하고, 수강생이 신청 → 결제 확정 → 취소 흐름을 통해 수강을 관리합니다.
정원 초과 시 대기열에 등록할 수 있으며, 수강 취소 시 대기자가 자동으로 PENDING 상태로 전환됩니다.

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| ORM | JPA (Hibernate) + Lombok |
| Database | MySQL 8.0 |
| Test | JUnit 5, CountDownLatch / ExecutorService |

---

## 실행 방법

### 사전 준비

- Docker 설치 필요

### 실행

```bash
# 1. MySQL 컨테이너 실행
docker-compose up -d mysql

# 2. 애플리케이션 실행
./gradlew bootRun
```

- 서버: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- DB: `localhost:3306` / DB명: `liveklass` / user: `liveklass` / pw: `liveklass1234`
- `ddl-auto: update` 설정으로 테이블 자동 생성

---

## API 목록 및 예시

인증은 `X-User-Id` 헤더로 대체합니다.

### User

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/users` | 테스트용 유저 생성 |

```json
// POST /users
Request:
{
  "name": "홍길동",
  "email": "hong@test.com",
  "role": "STUDENT"
}

Response 201:
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@test.com",
  "role": "STUDENT"
}
```

### Course

| Method | URL | 설명 | 헤더 |
|--------|-----|------|------|
| POST | `/courses` | 강의 등록 | X-User-Id (CREATOR) |
| PATCH | `/courses/{courseId}/status` | 상태 변경 | X-User-Id (CREATOR) |
| GET | `/courses` | 강의 목록 (상태 필터) | - |
| GET | `/courses/{courseId}` | 강의 상세 | - |
| GET | `/courses/{courseId}/enrollments` | 수강생 목록 (크리에이터 전용) | X-User-Id (CREATOR) |

```json
// POST /courses
Request:
{
  "title": "Spring Boot 입문",
  "description": "초보자를 위한 Spring Boot 강의",
  "price": 50000,
  "capacity": 30,
  "startDate": "2026-05-01",
  "endDate": "2026-06-30"
}

Response 201:
{
  "id": 1,
  "title": "Spring Boot 입문",
  "status": "DRAFT",
  "capacity": 30,
  "enrolledCount": 0
}

// PATCH /courses/1/status
Request: { "status": "OPEN" }
Response 200: { "id": 1, "status": "OPEN", ... }

// GET /courses?status=OPEN&page=0&size=20
Response 200:
{
  "content": [...],
  "totalElements": 5,
  "totalPages": 1
}

// GET /courses/1
Response 200:
{
  "id": 1,
  "title": "Spring Boot 입문",
  "capacity": 30,
  "enrolledCount": 12,
  "remainingCount": 18,
  "status": "OPEN"
}
```

### Enrollment

| Method | URL | 설명 | 헤더 |
|--------|-----|------|------|
| POST | `/courses/{courseId}/enrollments` | 수강 신청 | X-User-Id (STUDENT) |
| PATCH | `/courses/{courseId}/enrollments/confirm` | 결제 확정 | X-User-Id (STUDENT) |
| PATCH | `/courses/{courseId}/enrollments/cancel` | 수강 취소 | X-User-Id (STUDENT) |
| GET | `/enrollments/me` | 내 수강 신청 목록 | X-User-Id |

```json
// POST /courses/1/enrollments
Response 201:
{
  "id": 1,
  "courseId": 1,
  "status": "PENDING",
  "createdAt": "2026-04-10T10:00:00"
}
// Error 409: 정원 초과
// Error 409: 이미 신청한 강의
// Error 400: OPEN 상태가 아닌 강의

// PATCH /courses/1/enrollments/confirm
Response 200: { "id": 1, "status": "CONFIRMED", "confirmedAt": "2026-04-10T10:05:00" }

// PATCH /courses/1/enrollments/cancel
Response 200: { "id": 1, "status": "CANCELLED", "cancelledAt": "2026-04-10T10:10:00" }
// Error 400: CONFIRMED 후 7일 초과 시 취소 불가
```

### Waitlist

| Method | URL | 설명 | 헤더 |
|--------|-----|------|------|
| POST | `/courses/{courseId}/waitlists` | 대기 등록 | X-User-Id (STUDENT) |
| DELETE | `/courses/{courseId}/waitlists/me` | 대기 취소 | X-User-Id |
| GET | `/courses/{courseId}/waitlists/me` | 내 대기 순서 조회 | X-User-Id |

```json
// POST /courses/1/waitlists
Response 201:
{
  "courseId": 1,
  "waitOrder": 3,
  "totalWaiting": 5
}

// GET /courses/1/waitlists/me
Response 200:
{
  "waitOrder": 3,
  "totalWaiting": 5
}
```

---

## 데이터 모델 설명

### ERD

```
users
  id PK
  name, email (UNIQUE), role (CREATOR | STUDENT)

courses
  id PK
  creator_id FK → users.id
  title, description, price, capacity
  enrolled_count  -- PENDING + CONFIRMED 수 캐싱
  start_date, end_date
  status (DRAFT | OPEN | CLOSED)

enrollments
  id PK
  course_id FK → courses.id
  user_id FK → users.id
  status (PENDING | CONFIRMED | CANCELLED)
  confirmed_at, cancelled_at
  UNIQUE (course_id, user_id)

waitlists
  id PK
  course_id FK → courses.id
  user_id FK → users.id
  wait_order INT  -- 대기 순서 (MAX+1)
  UNIQUE (course_id, user_id)
```

### 상태 전이

**Course:** `DRAFT → OPEN → CLOSED` (크리에이터 수동 전환)

**Enrollment:**
```
PENDING → CONFIRMED → CANCELLED
PENDING → CANCELLED
```
- `PENDING`
  - 신청 완료, 결제 대기. 자리 점유
- `CONFIRMED`
  - 결제 완료, 수강 확정
- `CANCELLED`
  - PENDING은 언제든 취소 가능
  - CONFIRMED는 확정 후 7일 이내만 취소 가능

---

## 요구사항 해석 및 가정

- **Course 상태 전환 트리거 미명시** 
  - 크리에이터가 수동으로 전환하는 방식으로 구현. 정원 초과 시 `CLOSED` 자동 전환하지 않음.
- **PENDING도 정원에 포함** 
  - 결제 대기 중에도 자리를 점유하지 않으면 여러 명이 동시에 마지막 자리를 PENDING으로 점유할 수 있어 초과 확정 가능성 존재. 
  - PENDING도 정원에 포함해 방지.
- **대기열 수동 등록** 
  - 정원 초과 시 자동으로 대기열에 넣지 않고 사용자가 직접 대기 등록 API를 호출. 
  - 수강 신청은 결제가 수반되므로 사용자 의사 확인 없이 자동 대기 등록은 부적절.
- **confirm/cancel API 경로**
  - `enrollmentId` 대신 `courseId` 기반으로 설계. 
  - 사용자는 강의 상세 페이지에서 확정/취소하므로 `courseId`가 항상 자연스럽게 확보됨. 
  - `(course_id, user_id)` 유니크 제약으로 조합이 enrollment를 유일하게 식별.

---

## 설계 결정과 이유

### 동시성 처리 — Pessimistic Lock

수강 신청 시 `SELECT ... FOR UPDATE`로 Course 레코드에 비관적 쓰기 락을 적용.

**선택 이유:**
- 정원이 적은 강의에 동시 신청이 몰리는 경우 충돌 가능성이 높아 Optimistic Lock의 재시도 로직보다 직렬화가 명확함
- Optimistic Lock은 충돌 시 재시도 설계가 추가로 필요하고 재시도 중 또 실패할 수 있어 사용자 경험 저하 가능성 있음

### enrolled_count 캐싱

Course 엔티티에 `enrolled_count` 필드를 두어 현재 점유 인원을 캐싱.

**선택 이유:**
- 신청마다 `COUNT` 쿼리를 실행하는 것보다 락 범위가 명확하고 성능도 유리
- Course에 Pessimistic Lock이 걸려 있어 count 증감이 트랜잭션 내에서 안전하게 처리됨

### 취소 시 대기자 자동 전환

Enrollment 취소 트랜잭션 안에서 대기열 1순위를 조회해 자동으로 PENDING Enrollment 생성.

**선택 이유:**
- 취소와 전환이 같은 트랜잭션에서 처리되어 원자성 보장
- 전환 실패 시 취소 자체도 롤백되어 데이터 정합성 유지

### 재신청 허용

취소(CANCELLED) 후 동일 강의에 재신청 가능. 기존 CANCELLED 레코드를 PENDING으로 재활용.

**선택 이유:**
- `(course_id, user_id)` 유니크 제약을 유지하면서 재신청 허용
- 새 레코드 INSERT 시 unique 제약 위반 방지

---

## 테스트 실행 방법

```bash
# 테스트용 MySQL 컨테이너 실행 (port 3307)
docker-compose up -d mysql-test

# 테스트 실행
./gradlew test
```

### 동시성 테스트 (`EnrollmentConcurrencyTest`)

| 테스트 | 시나리오 | 검증 |
|--------|----------|------|
| `concurrentEnrollment_withCapacity1_onlyOneSucceeds` | 정원 1명 강의에 10명 동시 신청 | 성공 1건, enrolledCount = 1 |
| `concurrentEnrollment_withCapacity30_doesNotExceedCapacity` | 정원 30명 강의에 50명 동시 신청 | 성공 30건, enrolledCount = 30 |

- 실제 MySQL + `@SpringBootTest`로 동시 요청 시나리오 검증
- `CountDownLatch`로 모든 스레드가 동시에 출발하도록 제어

---

## 미구현 / 제약사항

- **PENDING 타임아웃 미구현** — 실제 환경에서는 결제 대기 시간 제한을 두고 스케줄러로 자동 취소 처리 필요
- **실제 결제 연동 없음** — confirm API 호출로 상태 변경만 처리
- **인증/인가 간략 처리** — `X-User-Id` 헤더로 인증 대체. `POST /users`로 테스트용 유저 생성 가능

---

## AI 활용 범위

Claude (claude-sonnet-4-6)를 활용하여 아래 작업을 진행했습니다.

- 엔티티 / ERD / API 설계 검토 및 의견 교환
- 동시성 처리 전략(Pessimistic Lock vs Optimistic Lock) 논의
- 코드 구현 검토 및 피드백(엔티티, 서비스, 컨트롤러, 리포지토리, 예외 처리)
- 설계 결정 사항에 대한 트레이드오프 분석
- 동시성 테스트 코드 작성 (CountDownLatch 기반 시나리오 설계)
- 버그 발견 및 수정 (재신청 시 unique 제약 위반 이슈)
- 커밋 메시지 및 PR 내용 작성
- 설계 문서 및 README 작성
