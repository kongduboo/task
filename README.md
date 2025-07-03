# 공지사항 관리 REST API

Java + Spring Boot 기반으로 구현한 공지사항 관리 REST API입니다. 공지사항의 등록, 조회, 수정, 삭제 기능을 제공하며, 대용량 트래픽에 대비한 조회수 캐싱, 검색 인덱스, 페이지네이션 등을 포함하고 있습니다.

---

## 프로젝트 개요

- **주제**: 공지사항 관리 REST API 구현
- **주요 기술 스택**:
    - Java 21
    - Spring Boot 3.x
    - Spring Data JPA (Hibernate)
    - QueryDSL
    - Redis
    - H2 Database
    - Gradle

---

## 기능 요약

### 1. 공지사항 API

- **등록**  
  입력 항목:
    - 제목
    - 내용
    - 공지 시작일시
    - 공지 종료일시
    - 첨부파일 (여러 개 가능)

- **조회**
    - 목록 응답: 제목, 첨부파일 유무, 등록일시, 조회수, 작성자
    - 상세 응답: 제목, 내용, 등록일시, 조회수, 작성자, 첨부파일 목록

- **수정**
    - 제목, 내용, 일정, 첨부파일 교체 가능

- **삭제**
    - 공지사항 삭제 처리

### 2. 검색 조건

- 검색어: 제목 + 내용 / 제목
- 검색기간: 등록일 기준 기간 필터링

---

## 실행 방법

### 1. Redis 실행 (필수)

조회수 캐싱 기능을 위해 Redis 컨테이너를 실행해야 합니다.

```bash
docker run --name local-redis -p 6379:6379 -d redis
```

### 2. 프로젝트 빌드 및 실행

```bash
./gradlew clean build
java -jar build/libs/task-0.0.1-SNAPSHOT.jar
```

---

## 테스트 방법

`src/test/resources` 디렉토리에 위치한 `.http` 파일을 이용하여 실제 API 호출 테스트를 수행할 수 있습니다.  

| 파일명 | 설명 |
|-------------------------------|----------------------------|
| `generated-requests_actuator.http` | Actuator 헬스 체크 등 시스템 상태 확인 |
| `generated-requests_등록.http`     | 공지사항 등록 API 테스트 |
| `generated-requests_수정.http`     | 공지사항 수정 API 테스트 |
| `generated-requests_삭제.http`     | 공지사항 삭제 API 테스트 |
| `generated-requests_조회.http`     | 공지사항 목록 및 상세 조회 API 테스트 |

> 테스트를 실행하기 전에 반드시 로컬 Redis 서버가 실행 중이어야 하며, `docker run --name local-redis -p 6379:6379 -d redis` 명령어로 컨테이너를 실행할 수 있습니다.

---

## 대용량 트래픽 고려 기능

공지사항 서비스는 대량의 요청 처리에 대비하여 다음과 같은 기능을 추가하였습니다.

### 조회수 캐싱 (Redis + 스케줄러)
- 실시간으로 증가하는 조회수는 Redis에 저장하고, 일정 주기로 DB에 반영합니다.
- 이를 통해 RDBMS에 직접 쓰는 작업을 최소화하여 성능을 향상시킵니다.

### 검색 최적화 (QueryDSL + 인덱스 설정)
- QueryDSL을 사용하여 복잡한 검색 조건을 동적으로 처리합니다.
- 테스트 환경(H2 DB)에서도 적절한 인덱스를 설정하여 성능을 확인했습니다.

### 페이지네이션 및 기본 정렬
- 공지사항 목록 조회 시 등록일시 기준 내림차순으로 기본 정렬합니다.
- 페이지 단위로 데이터를 조회하여 응답 크기를 제한하고 부하를 분산합니다.

---

## 요청 로깅 및 Trace ID 처리

운영 중 디버깅 및 로그 추적을 용이하게 하기 위해, 다음과 같은 요청 로깅 기능을 구현했습니다.

### 고유한 Trace ID 생성 및 로그 출력
- 각 API 요청마다 **고유한 Trace ID**를 생성합니다.
- 요청이 시작될 때 Trace ID를 `MDC(Mapped Diagnostic Context)`에 저장하고, 로그 포맷에 자동으로 포함됩니다.
- 로그 예시:

```
[2025-07-03 21:51:47.551] [3687613d-6b51-4664-9c0f-44a2187c116c] [http-nio-8080-exec-3] INFO  c.r.t.c.logging.ApiLoggingFilter
```

### API 요청 로깅 필터 구현
- 요청 URL, HTTP 메서드, 요청 IP, Trace ID 등의 정보를 출력하는 **서블릿 필터(Filter)**를 구현하였습니다.

```
[2025-07-03 21:57:56.330] [d45ad244-0b05-46f3-924b-f3f9c690a7b6] [http-nio-8080-exec-5] INFO  c.r.t.c.logging.ApiLoggingFilter - Incoming API Request: [POST] /api/v1/notices from 127.0.0.1
[2025-07-03 21:57:56.493] [d45ad244-0b05-46f3-924b-f3f9c690a7b6] [http-nio-8080-exec-5] INFO  c.r.t.c.logging.ApiLoggingFilter - Completed API Request: [POST] /api/v1/notices with status=200
```

- 응답 처리 후에는 `MDC.clear()`를 호출하여 컨텍스트를 정리합니다.

### 주요 효과
- Trace ID를 기준으로 한 요청 전체 흐름 추적 가능
- API Gateway, WAS, DB 로그 간 연계 분석이 가능하여 문제 발생 시 빠른 추적 가능
- 비정상 요청, 성능 병목, 장애 상황에 대한 빠른 파악 지원

### 참고 로그 설정 예시 (logback)
```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId}] %-5level %logger - %msg%n</pattern>
```

---

## 테스트 및 품질

### 테스트 종류
- **단위 테스트(Unit Test)**: 서비스 계층의 핵심 비즈니스 로직 검증
- **통합 테스트(Integration Test)**: 컨트롤러 및 전체 API 흐름 검증

### 테스트 도구
- JUnit 5
- Spring Boot Test

### 테스트 대상
- API 정상 흐름 및 주요 예외 상황
- 첨부파일 업로드 및 다운로드 기능
- 검색 및 페이징 처리
- 조회수 증가 로직과 Redis 반영 여부

> 모든 테스트는 `./gradlew test` 명령어를 통해 실행할 수 있으며, 필요한 경우 테스트 결과 리포트를 추가 설정할 수 있습니다.