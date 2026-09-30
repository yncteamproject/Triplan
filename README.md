# Triplan — 여행 플래너 웹 서비스

여행 일정(방문지 · 이동 수단 · 숙소)을 짜면 경비가 자동으로 합산되고, 완성한 여행 계획을 공유 게시판에 올려 다른 사람과 나눌 수 있는 웹 서비스입니다.
2026년 영남이공대학교 3학년 일취반 팀 프로젝트로 제작하고 있습니다.

## 📌 필독 — 설계 문서

> [!IMPORTANT]
> 코드를 보기 전에 아래 문서를 먼저 읽어 주세요. 기능 ID(M/T/S/P)와 규칙 ID(B1~B10)는 커밋 메시지 · PR · 리뷰에서 그대로 사용합니다.

| 문서 | 내용 |
|------|------|
| [요구사항 명세](docs/requirements.md) | 기능별 입력 · 처리 · 인수 기준, 비즈니스 규칙, 결정 사항, 알려진 문제 |
| [API 명세](docs/api.md) | 엔드포인트, 로그인 필요 여부, 요청 · 응답 형식, 에러 응답 |
| [ERD](docs/erd.md) | 테이블 8개, 관계, 제약 조건, 규칙과 데이터의 연결 |

코드와 함께 바뀌는 내용(API, DB, 실행 방법, 규칙)은 이 저장소에, 일정 · 회의록 · 할 일은 노션에서 관리합니다.

| 협업 도구 | 용도 |
|-----------|------|
| [노션 — 팀 프로젝트](https://app.notion.com/p/671d327294924936916e5f28851598b6) | 기능 목록, 회의록, 공지, 주간 진행 |
| [Figma — 페이지 디자인](https://www.figma.com/design/ndkdnGp1QYxBRt6Oiz6G2w) | 화면 디자인 초안 |

## 기술 스택

| 구분 | 내용 |
|------|------|
| 백엔드 | Spring Boot 4.0.7, Java 21, Spring Data JPA, Bean Validation, Lombok |
| DB | PostgreSQL (Neon) |
| 인증 | Spring Security + JWT (jjwt 0.12.6, 토큰 유효기간 24시간, BCrypt) |
| 프론트엔드 | React 19, Vite 8, axios |
| 테스트 | JUnit 5, MockMvc, spring-security-test (실제 DB + `@Transactional` 롤백) |
| 빌드 | Gradle (백엔드), npm (프론트엔드) |
| 외부 API (예정) | 카카오맵 JS SDK, 오디세이(ODsay), 한국수출입은행 환율 |

## 실행 방법

### 1. 준비물
- JDK 21 (Gradle 툴체인이 21로 빌드합니다)
- Node.js 22 이상

### 2. 백엔드 설정
DB 접속 정보는 git에 올리지 않고 `backend/src/main/resources/application-secret.yaml`에 따로 둡니다. 파일을 만들고 값은 팀장에게 받아서 채워 주세요.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://{호스트}/{DB이름}?sslmode=require
    username: {계정}
    password: {비밀번호}
    driver-class-name: org.postgresql.Driver
```

> [!NOTE]
> 자기 컴퓨터에서만 H2 같은 다른 DB를 쓰고 싶다면 이 파일에 설정하세요. `application.yaml`은 모두가 같이 쓰므로 바꾸지 않습니다.

### 3. 백엔드 실행
```bash
cd backend
./gradlew bootRun
```
Windows에서는 `gradlew.bat bootRun`. 실행 후 http://localhost:8080 에서 API가 열립니다.
테이블은 `ddl-auto: update`로 자동 생성 · 변경됩니다.

### 4. 프론트엔드 실행
```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```
http://localhost:5173 에서 열립니다. 백엔드 주소는 `.env`의 `VITE_API_BASE_URL`로 바꿀 수 있고, 백엔드는 이 주소(5173)의 요청만 CORS로 허용합니다.

### 5. 테스트
```bash
cd backend
./gradlew test
```
테스트는 `application-secret.yaml`의 DB에 실제로 붙지만, 모든 테스트가 `@Transactional`이라 끝나면 롤백되어 데이터가 남지 않습니다.

## 프로젝트 구조

```
backend/src/main/java/kr/ync/triplan/
├─ TriplanApplication.java
├─ global/                         # 모든 기능이 같이 쓰는 것
│  ├─ config/     SecurityConfig(URL별 접근 규칙, 401/403 응답), WebConfig(CORS)
│  ├─ jwt/        JwtTokenProvider, JwtAuthenticationFilter
│  └─ exception/  CustomException, ErrorResponse, GlobalExceptionHandler,
│                 CommonExceptionHandler, ForbiddenException
├─ member/                         # 회원 · 인증 · 마이페이지 (하위 구성은 아래 기능 모두 동일)
│  ├─ controller/  AuthController, MemberController
│  ├─ domain/      Member, Role
│  ├─ dto/request, dto/response
│  ├─ exception/
│  ├─ repository/
│  └─ service/     MemberService(Impl), MyPageService
├─ trip/                           # 여행 계획 (Trip + Stop + TransportSegment + Lodging)
├─ share/                          # 공유 게시판 (SharePage + Comment)
└─ travelTest/                     # 여행 성향 테스트

backend/src/test/java/kr/ync/triplan/   # main과 같은 패키지 구조
└─ support/BaseController.java          # MockMvc + Security, 로그인 사용자 · 토큰 헬퍼

frontend/src/
└─ api/   client.js(토큰 자동 첨부), authApi.js, tripApi.js
```

새 기능은 **데이터 종류(도메인)** 기준으로 패키지를 나눕니다. 화면(페이지) 기준으로 나누지 않습니다.
예를 들어 마이페이지 화면은 `member`(내 정보) · `trip`(내 여행) · `share`(내 글)의 API를 가져다 씁니다.

## 기능 구현 현황

ID와 인수 기준은 [요구사항 명세](docs/requirements.md)를 참고하세요.

### 회원 (M)
| ID | 기능 | 상태 |
|----|------|------|
| M1 | 회원가입 | ✅ 완료 |
| M2 | 로그인 (JWT 발급) | ✅ 완료 |
| M3 | 내 정보 조회 | ✅ 완료 |
| M4 | 내 정보 수정 (닉네임 · 비밀번호) | ✅ 완료 ([알려진 문제](docs/requirements.md#알려진-문제) 1건) |

### 여행 계획 (T)
| ID | 기능 | 상태 |
|----|------|------|
| T1 | 여행 계획 생성 · 조회 · 수정 · 삭제 | ✅ 완료 |
| T2 | 방문지(Stop) 관리 · 순서 | ✅ 완료 |
| T3 | 구간별 이동 수단 · 시간 · 예약 정보 | ✅ 완료 |
| T4 | 숙소 (체크인 · 체크아웃 · 숙박비) | ✅ 완료 |
| T5 | 자동 견적 합산 (교통비 + 숙박비) | ✅ 완료 |
| T6 | 지도 시각화 (마커 + 경로, 카카오맵) | ⏳ 예정 (API 키 필요) |
| T7 | 대중교통 경로 · 소요시간 (오디세이) | ⏳ 예정 (API 키 필요) |
| T8 | 환율 표시 (한국수출입은행) | ⏳ 예정 (API 키 필요) |

### 공유 게시판 (S)
| ID | 기능 | 상태 |
|----|------|------|
| S1 | 게시글 작성 · 조회 · 수정 · 삭제 (조회수) | ✅ 완료 |
| S2 | 댓글 작성 · 조회 · 삭제 | ✅ 완료 (수정은 예정) |
| S3 | 게시글 목록 페이징 | ⏳ 예정 |

### 여행 성향 테스트 (P)
| ID | 기능 | 상태 |
|----|------|------|
| P1 | 성향 테스트 제출 · 결과 계산 | ✅ 완료 |
| P2 | 내 최근 결과 조회 | ✅ 완료 |

### 화면 (프론트엔드)
| 기능 | 상태 |
|------|------|
| Vite + React 기본 구조, API 클라이언트 | ✅ 완료 |
| 각 페이지 화면 | ⏳ 예정 (Figma 기준) |

## 비즈니스 규칙

접근 권한(로그인 · 본인 확인)과 값 검증 규칙 10개(B1~B10)는 모두 화면이 아니라 서버에서 검사합니다.
목록과 위반 시 응답은 [요구사항 명세 — 비즈니스 규칙](docs/requirements.md#비즈니스-규칙)에 있습니다.

## 팀원 · 담당

2명이 진행합니다.

| 이름 | 담당 | 주요 브랜치 |
|------|------|-------------|
| [daengsuk2](https://github.com/daengsuk2) | 팀장, 여행 플래너, 공유 게시판, 인증 · 인가 | `feature/mainPage`, `feature/sharePage` |
| [yoonhyoguen](https://github.com/yoonhyoguen) | 회원, 마이페이지, 회원 정보 수정, 여행 성향 테스트 | `feature/users`, `feature/myPage`, `feature/updateUser` |

## 협업 규칙

### 브랜치 · PR
1. 작업 전에 `main`을 받아 최신 상태에서 시작합니다.
2. 브랜치 이름: `feature/{기능}`, `fix/{내용}`, `docs/{내용}`
3. PR을 올리기 전에 `main`을 자기 브랜치에 한 번 더 합쳐서 충돌을 먼저 해결합니다.
4. `main`에는 직접 push하지 않고 PR → 리뷰 → merge 순서로 합칩니다.
5. PR 본문에는 주요 변경, 팀원이 확인할 점, 테스트 결과를 적습니다.

### 커밋 메시지
```
제목 (변경한 주요 파일)

- 파일/기능: 무엇을 왜 바꿨는지
```

### 테스트
- Controller · Service · Repository 계층별로 테스트를 작성합니다.
- 생성 API는 4가지 입력을 검증합니다: 정상 데이터 / 필수 데이터 누락 / null 값 / 비정상 데이터.
- 로그인이 필요한 API는 비로그인(401), 남의 데이터(403) 케이스를 같이 확인합니다.
- 테스트 메서드 이름은 영어로 쓰고, 설명은 `@DisplayName`에 한글로 씁니다.

현재 테스트: **169개 통과**

## 배포 전 확인할 것

- `application.yaml`의 `jwt.secret`은 개발용 값입니다. 배포 전에 새 키를 만들어 `application-secret.yaml`로 옮겨야 합니다. 이 키를 아는 사람은 로그인 토큰을 위조할 수 있습니다.
- CORS 허용 주소(`WebConfig`)를 실제 프론트엔드 주소로 바꿔야 합니다.
