# ERD

테이블은 JPA 엔티티에서 `ddl-auto: update`로 자동 생성됩니다.
컬럼이나 관계를 바꾸면 이 문서도 같은 PR에서 같이 고칩니다.

```mermaid
erDiagram
    MEMBER ||--o{ TRIP : "소유"
    TRIP ||--o{ STOP : "방문지"
    TRIP ||--o{ TRANSPORT_SEGMENT : "이동 구간"
    TRIP ||--o{ LODGING : "숙소"
    STOP ||--o{ TRANSPORT_SEGMENT : "출발지 (from_stop_id)"
    STOP ||--o{ TRANSPORT_SEGMENT : "도착지 (to_stop_id)"
    TRIP ||--o{ SHARE_PAGE : "공유"
    MEMBER ||--o{ SHARE_PAGE : "작성"
    SHARE_PAGE ||--o{ COMMENT : "댓글"
    MEMBER ||--o{ COMMENT : "작성"
    MEMBER ||--o{ TRAVEL_PREFERENCE_RESULT : "테스트 결과"

    MEMBER {
        bigint id PK
        varchar email UK "로그인 ID"
        varchar password "BCrypt"
        varchar nickname
        varchar role "USER / ADMIN"
        timestamp created_at
    }
    TRIP {
        bigint id PK
        bigint member_id FK "member"
        varchar title
        date start_date
        date end_date
    }
    STOP {
        bigint id PK
        bigint trip_id FK
        varchar name
        date date
        time time "nullable"
        varchar memo "nullable"
        varchar image_url "nullable"
        int stop_order "nullable"
    }
    TRANSPORT_SEGMENT {
        bigint id PK
        bigint trip_id FK
        bigint from_stop_id FK "stop"
        bigint to_stop_id FK "stop"
        varchar mode "FLIGHT / TRAIN / BUS / CAR / WALK"
        timestamp depart_time
        timestamp arrive_time
        int cost "nullable"
        varchar reservation_no "nullable"
    }
    LODGING {
        bigint id PK
        bigint trip_id FK
        varchar name
        timestamp check_in
        timestamp check_out
        int cost "nullable"
        varchar reservation_no "nullable"
    }
    SHARE_PAGE {
        bigint id PK
        bigint trip_id FK
        bigint member_id FK "작성자"
        varchar title
        varchar description "nullable, 255자 (알려진 문제)"
        timestamp write_date
        timestamp update_date "nullable"
        int view_count
    }
    COMMENT {
        bigint id PK
        bigint share_page_id FK
        bigint member_id FK "작성자"
        varchar content "255자 (알려진 문제)"
        timestamp created_at
    }
    TRAVEL_PREFERENCE_RESULT {
        bigint id PK
        bigint member_id FK "nullable"
        varchar travel_type "4가지 유형"
        timestamp tested_at
    }
```

## 테이블 요약

| 테이블 | 엔티티 | 패키지 | 설명 |
|--------|--------|--------|------|
| member | Member | member | 회원. 로그인 ID는 이메일 |
| trip | Trip | trip | 여행 계획 (여행의 주인 = member) |
| stop | Stop | trip | 여행의 방문지, `stop_order`로 순서 |
| transport_segment | TransportSegment | trip | 방문지 → 방문지 이동 구간 |
| lodging | Lodging | trip | 여행의 숙소 |
| share_page | SharePage | share | 여행 계획을 공유한 게시글 |
| comment | Comment | share | 게시글 댓글 |
| travel_preference_result | TravelPreferenceResult | travelTest | 여행 성향 테스트 결과 (여러 번 하면 여러 건) |

## 제약 조건

| 테이블 | 제약 | 목적 |
|--------|------|------|
| member | UNIQUE(email) | 같은 이메일 중복 가입 방지 (B8) |
| trip, share_page, comment | member_id NOT NULL | 주인 · 작성자가 없는 데이터 방지 |
| stop, transport_segment, lodging | trip_id NOT NULL | 여행에 속하지 않는 데이터 방지 |
| transport_segment | from_stop_id, to_stop_id NOT NULL | 출발지 · 도착지 필수 |

값 범위(날짜 순서, 0 이상)는 DB 제약이 아니라 서버 검증으로 막습니다 (B5~B7).

## 관계 요약
- member 1 : N trip, trip 1 : N stop / transport_segment / lodging
- stop 1 : N transport_segment (출발지, 도착지 각각)
- trip 1 : N share_page, share_page 1 : N comment
- member 1 : N share_page / comment (작성자), member 1 : N travel_preference_result

## 규칙과 데이터의 연결

| 규칙 | 데이터에서 확인하는 방법 |
|------|--------------------------|
| B2 | 요청한 사용자 이메일 = `trip.member_id`의 `member.email` (하위 데이터는 `trip_id`로 거슬러 올라가 확인) |
| B3 | 요청한 사용자 이메일 = `share_page.member_id` / `comment.member_id`의 `member.email` |
| B4 | 공유하려는 `trip.member_id` = 로그인한 사용자 |
| B5 | `trip.start_date ≤ trip.end_date` |
| B6 | `depart_time ≤ arrive_time`, `check_in ≤ check_out` |
| B7 | `cost ≥ 0`, `stop_order ≥ 0` |
| B8 | `member.email` UNIQUE |
| B10 | `transport_segment.from_stop_id` · `to_stop_id`의 `stop.trip_id`가 본인 여행 |

## 삭제할 때 주의

하위 데이터가 있는 행을 삭제하면 외래키 때문에 실패합니다 (현재 500 에러, [알려진 문제](requirements.md#알려진-문제)).

| 삭제 대상 | 막는 데이터 |
|-----------|-------------|
| trip | stop, transport_segment, lodging, share_page |
| stop | 이 방문지를 출발지 · 도착지로 쓰는 transport_segment |
| share_page | comment |

## 변경 이력

| 날짜 | 변경 | 이유 |
|------|------|------|
| 2026-09-30 | `trip.user_id`, `share_page.writer_id`, `comment.writer_id`(문자열) 삭제 → `member_id`(FK)로 교체 | 로그인한 회원과 연결해서 본인 확인(B2 · B3)을 하기 위해 |
