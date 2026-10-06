# Triplan — 여행 플래너 웹 서비스

여행 일정(방문지 · 이동 수단 · 숙소)을 짜면 경비가 자동으로 합산되고, 완성한 여행 계획을 공유 게시판에 올려 다른 사람과 나눌 수 있는 웹 서비스입니다.
2026년 영남이공대학교 3학년 일취반 팀 프로젝트로 제작하고 있습니다.

## 📌 필독 — 설계 문서

> [!IMPORTANT]
> 코드를 보기 전에 아래 문서를 먼저 읽어 주세요. 기능 ID(M/T/S/P)와 규칙 ID(B1~B12)는 커밋 메시지 · PR · 리뷰에서 그대로 사용합니다.

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
DB 접속 정보 및 JWT 비밀키는 git에 올리지 않고 `backend/src/main/resources/application-secret.yaml`에 따로 둡니다. 파일을 만들고 값은 팀장에게 받아서 채워 주세요.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://{호스트}/{DB이름}?sslmode=require
    username: {계정}
    password: {비밀번호}
    driver-class-name: org.postgresql.Driver

jwt:
  secret_key: {Base64 비밀키, 32바이트 이상}
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

> **Windows에서 `wrong name` 에러가 나면** 패키지 이름의 대소문자만 바뀐 경우(예: `travelTest` → `traveltest`)입니다. Windows는 폴더 이름의 대소문자를 구분하지 않아 이전 빌드 결과물 폴더가 그대로 남기 때문입니다. `./gradlew clean test`로 이전 빌드 결과물을 지우고 다시 실행하세요.

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
└─ traveltest/                     # 여행 성향 테스트

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
| M4 | 내 정보 수정 (닉네임 · 비밀번호) | ✅ 완료 |

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
| S4 | 공유된 여행 상세 보기 (방문지 · 이동 · 숙소 · 경비, 예약번호 제외) | ✅ 완료 |
| S5 | 공유된 여행을 내 여행으로 복사 + 복사 허용 설정 | ⏳ 예정 |

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

접근 권한(로그인 · 본인 확인)과 값 검증 규칙(B1~B12)은 모두 화면이 아니라 서버에서 검사합니다.
목록과 위반 시 응답은 [요구사항 명세 — 비즈니스 규칙](docs/requirements.md#비즈니스-규칙)에 있습니다.

## 팀원 · 담당

2명이 진행합니다.

| 이름 | 담당 | 이슈 라벨 |
|------|------|-----------|
| [daengsuk2](https://github.com/daengsuk2) | 팀장, 여행 플래너, 공유 게시판 | `영역: 여행 계획`, `영역: 공유 게시판` |
| [yoonhyoguen](https://github.com/yoonhyoguen) | 회원, 마이페이지, 회원 정보 수정, 여행 성향 테스트 | `영역: 회원`, `영역: 성향 테스트` |

**인증 · 인가(로그인 · JWT · 권한 검사)와 공통 설정은 2명이 같이 담당합니다.**
`영역: 공통` 이슈는 담당자를 2명 모두 지정하고, 리뷰는 작업하지 않은 사람이 합니다.

## 협업 규칙

### 작업 흐름

```
이슈 확인 → main 최신화 → 브랜치 생성 → 작업 + 테스트 + 문서 → PR (Closes #번호) → 리뷰 → merge → 브랜치 삭제 → 노션 상태 변경
```

1. **할 일은 이슈로 시작합니다.** 새 작업이나 버그는 GitHub Issues에 먼저 올리고, 담당자와 라벨(종류 · 영역)을 붙입니다.
2. **이슈 하나당 브랜치 하나.** 작업이 끝나면 브랜치를 지우고, 다음 작업은 최신 `main`에서 새로 만듭니다. 한 브랜치를 오래 쓰지 않습니다.
3. **`main`에는 직접 push하지 않습니다.** 반드시 PR → 리뷰 → merge 순서로 합칩니다.
4. **리뷰는 서로 한 번씩.** 상대방 PR은 Files changed를 보고 Approve 또는 코멘트를 남깁니다.
5. **문서는 같은 PR에서 고칩니다.** 이슈를 해결하는 PR에 관련 문서 수정까지 포함하고, 문서만 고치는 PR을 따로 만들지 않습니다.

   | 이런 걸 했으면 | 이 문서를 고침 |
   |------|------|
   | 기능 추가 · 완료 | `README.md` 기능 표 상태, `docs/requirements.md` 기능 설명 |
   | API 추가 · 변경 | `docs/api.md` |
   | DB 테이블 · 컬럼 변경 | `docs/erd.md` |
   | 알려진 문제(K) 해결 | `docs/requirements.md` 알려진 문제 상태 ✅ |
   | 규칙(B) 추가 · 변경 | `docs/requirements.md` 비즈니스 규칙 |

### 브랜치 이름

```
{종류}/{이슈번호}-{짧은-설명}
```

| 종류 | 이슈 라벨 | 예시 |
|------|-----------|------|
| `feature` | `enhancement` | `feature/12-login-page` |
| `fix` | `bug` | `fix/7-delete-cascade` |
| `refactor` | `refactor` | `refactor/11-traveltest-package` |
| `docs` | `documentation` | `docs/13-api-update` |

- 소문자와 하이픈(`-`)만 씁니다.
- 이슈가 없는 작은 작업은 번호를 생략해도 됩니다 (예: `docs/branch-rules`).

### 명령어로 보기

```bash
# 1. 최신 main에서 시작
git checkout main
git pull origin main
git checkout -b fix/7-delete-cascade

# 2. 작업하고 커밋 (여러 번 해도 됨)
git add {바꾼 파일}
git commit

# 3. PR 올리기 전에 main 변경을 한 번 더 받아서 충돌 해결
git fetch origin
git merge origin/main

# 4. 올리고 GitHub에서 PR 생성
git push -u origin fix/7-delete-cascade
```

### PR
- PR을 열면 템플릿(`.github/pull_request_template.md`)이 자동으로 채워집니다. 항목을 채우고 체크리스트를 확인합니다.
- 관련 이슈는 `Closes #번호`로 적습니다. merge되면 이슈가 자동으로 닫힙니다.
- merge한 뒤에는 원격 브랜치를 삭제하고, 노션 기능 목록의 상태를 바꿉니다.

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
- `./gradlew test`가 모두 통과해야 PR을 올립니다.

## 배포 전 확인할 것

- `application-secret.yaml`의 `jwt.secret_key`는 개발용입니다. 배포 서버에는 키를 새로 만들어 넣어야 합니다.
- CORS 허용 주소(`WebConfig`)를 실제 프론트엔드 주소로 바꿔야 합니다.
