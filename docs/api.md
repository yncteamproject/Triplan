# API 명세

백엔드 기본 주소: `http://localhost:8080`
기능 ID(M/T/S/P)와 규칙 ID(B1~B12)는 [요구사항 명세](requirements.md)와 같습니다.

## 공통

### 인증
로그인(`POST /api/auth/login`)으로 받은 `accessToken`을 요청 헤더에 붙입니다.

```
Authorization: Bearer {accessToken}
```

- 프론트엔드는 `frontend/src/api/client.js`가 `localStorage`의 `accessToken`을 자동으로 붙입니다.
- 토큰 유효기간은 24시간입니다.
- 🔓 표시가 있는 API만 토큰 없이 호출할 수 있고, 나머지는 모두 로그인이 필요합니다 (B1).

### 응답 코드

| 코드 | 의미 |
|------|------|
| 200 | 조회 · 수정 성공 |
| 201 | 생성 성공 |
| 204 | 삭제 성공 (본문 없음) |
| 400 | 입력값 검증 실패, 요청 본문 형식 오류 |
| 401 | 로그인 필요, 로그인 실패 |
| 403 | 본인 데이터가 아님 |
| 404 | 대상이 없음 |
| 409 | 이미 가입된 이메일 |

### 에러 응답 형식
모든 에러는 같은 형식으로 옵니다.

```json
{
  "timestamp": "2026-09-30T14:00:00",
  "status": 403,
  "message": "접근 권한이 없습니다."
}
```

입력값 검증이 여러 개 실패하면 그중 첫 번째 메시지만 옵니다.

### 날짜 · 시간 형식

| 종류 | 형식 | 예시 |
|------|------|------|
| 날짜 | `yyyy-MM-dd` | `2026-10-01` |
| 시간 | `HH:mm:ss` | `09:00:00` |
| 날짜+시간 | `yyyy-MM-ddTHH:mm:ss` | `2026-10-01T09:00:00` |

## 전체 목록

| 기능 | 메서드 | URL | 인증 | 성공 |
|------|--------|-----|:----:|:----:|
| M1 | POST | `/api/auth/signup` | 🔓 | 201 |
| M2 | POST | `/api/auth/login` | 🔓 | 200 |
| M3 | GET | `/api/members/me` | 🔒 | 200 |
| M4 | PUT | `/api/members/me` | 🔒 | 200 |
| T1 | POST | `/api/trips` | 🔒 | 201 |
| T1 | GET | `/api/trips` | 🔒 | 200 |
| T1 | GET | `/api/trips/{id}` | 🔒 본인 | 200 |
| T1 | PUT | `/api/trips/{id}` | 🔒 본인 | 200 |
| T1 | DELETE | `/api/trips/{id}` | 🔒 본인 | 204 |
| T5 | GET | `/api/trips/{id}/estimate` | 🔒 본인 | 200 |
| T2 | POST | `/api/trips/{tripId}/stops` | 🔒 본인 | 201 |
| T2 | GET | `/api/trips/{tripId}/stops` | 🔒 본인 | 200 |
| T2 | GET | `/api/stops/{id}` | 🔒 본인 | 200 |
| T2 | PUT | `/api/stops/{id}` | 🔒 본인 | 200 |
| T2 | DELETE | `/api/stops/{id}` | 🔒 본인 | 204 |
| T3 | POST | `/api/trips/{tripId}/transport-segments` | 🔒 본인 | 201 |
| T3 | GET | `/api/trips/{tripId}/transport-segments` | 🔒 본인 | 200 |
| T3 | GET | `/api/transport-segments/{id}` | 🔒 본인 | 200 |
| T3 | PUT | `/api/transport-segments/{id}` | 🔒 본인 | 200 |
| T3 | DELETE | `/api/transport-segments/{id}` | 🔒 본인 | 204 |
| T4 | POST | `/api/trips/{tripId}/lodgings` | 🔒 본인 | 201 |
| T4 | GET | `/api/trips/{tripId}/lodgings` | 🔒 본인 | 200 |
| T4 | GET | `/api/lodgings/{id}` | 🔒 본인 | 200 |
| T4 | PUT | `/api/lodgings/{id}` | 🔒 본인 | 200 |
| T4 | DELETE | `/api/lodgings/{id}` | 🔒 본인 | 204 |
| S1 | POST | `/api/share-pages` | 🔒 | 201 |
| S1 | GET | `/api/share-pages` | 🔓 | 200 |
| S1 | GET | `/api/share-pages/{id}` | 🔓 | 200 |
| S1 | PUT | `/api/share-pages/{id}` | 🔒 작성자 | 200 |
| S1 | DELETE | `/api/share-pages/{id}` | 🔒 작성자 | 204 |
| S4 | GET | `/api/share-pages/{id}/trip` | 🔓 | 200 |
| S5 | POST | `/api/share-pages/{id}/copy` | 🔒 | 201 |
| S2 | POST | `/api/share-pages/{sharePageId}/comments` | 🔒 | 201 |
| S2 | GET | `/api/share-pages/{sharePageId}/comments` | 🔓 | 200 |
| S2 | DELETE | `/api/comments/{commentId}` | 🔒 작성자 | 204 |
| P1 | POST | `/api/travel-test` | 🔒 | 200 |
| P2 | GET | `/api/travel-test/me` | 🔒 | 200 |

🔓 로그인 없이 가능 · 🔒 로그인 필요 · 🔒 본인/작성자 = 로그인 + 본인 데이터만 (아니면 403)

---

## 회원 (M)

### M1 회원가입 — `POST /api/auth/signup` 🔓

| 필드 | 타입 | 필수 | 검증 |
|------|------|:----:|------|
| email | string | ✅ | 이메일 형식 |
| password | string | ✅ | 8~20자 |
| nickname | string | ✅ | |

```json
// 201
{ "id": 1, "email": "hong@test.com", "nickname": "홍길동" }
```
- 409: 이미 가입된 이메일 (B8)

### M2 로그인 — `POST /api/auth/login` 🔓

| 필드 | 타입 | 필수 |
|------|------|:----:|
| email | string | ✅ |
| password | string | ✅ |

```json
// 200
{ "accessToken": "eyJhbGciOi...", "memberId": 1, "nickname": "홍길동" }
```
- 401: 이메일 또는 비밀번호가 틀림 (둘을 구분하지 않음, B9)
- `memberId`는 게시글 · 댓글의 `writerId`와 비교해서 "내 글" 여부를 판단할 때 씁니다.

### M3 내 정보 조회 — `GET /api/members/me` 🔒

```json
// 200
{ "id": 1, "email": "hong@test.com", "nickname": "홍길동", "role": "USER", "createdAt": "2026-09-30T14:00:00" }
```

### M4 내 정보 수정 — `PUT /api/members/me` 🔒

| 필드 | 타입 | 필수 | 검증 |
|------|------|:----:|------|
| nickname | string | | 20자 이내 |
| password | string | | 8~20자 |

- 보내지 않은(null) 항목은 기존 값을 유지합니다.
- 빈 문자열 `""`이나 공백만 보내도 보내지 않은 것으로 처리해 기존 값을 유지합니다.
- 응답은 M3과 같습니다.

---

## 여행 계획 (T)

여행 계획과 그 아래 방문지 · 이동 구간 · 숙소 · 견적은 **여행 주인만** 조회 · 수정 · 삭제할 수 있습니다 (B2).

### T1 여행 계획

**생성 `POST /api/trips` · 수정 `PUT /api/trips/{id}`**

| 필드 | 타입 | 필수 | 검증 |
|------|------|:----:|------|
| title | string | ✅ | |
| startDate | 날짜 | ✅ | |
| endDate | 날짜 | ✅ | startDate 이후 (B5) |

```json
// 201 / 200
{ "id": 1, "title": "제주도 여행", "startDate": "2026-10-01", "endDate": "2026-10-04" }
```

**목록 `GET /api/trips`**: 로그인한 사용자의 여행만 배열로 반환합니다.

**삭제 `DELETE /api/trips/{id}`**: 방문지 · 이동 구간 · 숙소와, 이 여행을 공유한 게시글(댓글 포함)도 함께 삭제됩니다.

### T5 자동 견적 — `GET /api/trips/{id}/estimate`

이동 구간 비용과 숙소 비용을 합산합니다. 비용이 비어 있는 항목은 0원으로 계산합니다.

```json
// 200
{ "tripId": 1, "transportCost": 20000, "lodgingCost": 100000, "totalCost": 120000 }
```

### T2 방문지

**생성 `POST /api/trips/{tripId}/stops` · 수정 `PUT /api/stops/{id}`**

| 필드 | 타입 | 필수 | 검증 |
|------|------|:----:|------|
| name | string | ✅ | |
| date | 날짜 | ✅ | |
| time | 시간 | | |
| memo | string | | |
| imageUrl | string | | |
| stopOrder | number | | 0 이상 (B7) |

```json
// 201 / 200
{ "id": 1, "tripId": 1, "name": "성산일출봉", "date": "2026-10-01", "time": "09:00:00",
  "memo": "일출 명소", "imageUrl": null, "stopOrder": 1 }
```

**목록 `GET /api/trips/{tripId}/stops`**: `stopOrder` 오름차순으로 반환합니다.

**삭제 `DELETE /api/stops/{id}`**: 이 방문지를 출발지 · 도착지로 쓰는 이동 구간도 함께 삭제됩니다.

### T3 이동 구간

**생성 `POST /api/trips/{tripId}/transport-segments` · 수정 `PUT /api/transport-segments/{id}`**

| 필드 | 타입 | 필수 | 검증 |
|------|------|:----:|------|
| fromStopId | number | ✅ | 본인 여행의 방문지 (B10) |
| toStopId | number | ✅ | 본인 여행의 방문지 (B10) |
| mode | string | ✅ | `FLIGHT` · `TRAIN` · `BUS` · `CAR` · `WALK` |
| departTime | 날짜+시간 | ✅ | |
| arriveTime | 날짜+시간 | ✅ | departTime 이후 (B6) |
| cost | number | | 0 이상 (B7) |
| reservationNo | string | | |

```json
// 201 / 200
{ "id": 1, "tripId": 1, "fromStopId": 1, "toStopId": 2, "mode": "CAR",
  "departTime": "2026-10-01T09:00:00", "arriveTime": "2026-10-01T10:00:00",
  "cost": 20000, "reservationNo": "RES123" }
```

### T4 숙소

**생성 `POST /api/trips/{tripId}/lodgings` · 수정 `PUT /api/lodgings/{id}`**

| 필드 | 타입 | 필수 | 검증 |
|------|------|:----:|------|
| name | string | ✅ | |
| checkIn | 날짜+시간 | ✅ | |
| checkOut | 날짜+시간 | ✅ | checkIn 이후 (B6) |
| cost | number | | 0 이상 (B7) |
| reservationNo | string | | |

```json
// 201 / 200
{ "id": 1, "tripId": 1, "name": "제주 호텔", "checkIn": "2026-10-01T15:00:00",
  "checkOut": "2026-10-02T11:00:00", "cost": 100000, "reservationNo": "RES123" }
```

---

## 공유 게시판 (S)

목록 · 상세 · 댓글 목록 · 공유된 여행 상세는 로그인 없이 볼 수 있고, 작성은 로그인, 수정 · 삭제는 작성자만 가능합니다 (B3).

### S1 게시글

**생성 `POST /api/share-pages`**

| 필드 | 타입 | 필수 | 검증 |
|------|------|:----:|------|
| title | string | ✅ | 100자 이내 |
| description | string | | 2000자 이내 |
| tripId | number | ✅ | 본인 여행만 공유 가능 (B4) |
| allowCopy | boolean | | 다른 사람의 복사 허용 여부. 안 보내면 `true` (B11) |

**수정 `PUT /api/share-pages/{id}`**: `title`, `description`, `allowCopy`를 바꿀 수 있습니다 (검증은 생성과 같음). `allowCopy`를 안 보내면 기존 값을 유지합니다.

**상세 `GET /api/share-pages/{id}`** 🔓: 조회할 때마다 `viewCount`가 1 오릅니다.

```json
// 상세 · 생성 · 수정 응답
{ "id": 1, "title": "제주도 3박 4일", "description": "여행 후기", "tripId": 1,
  "writerId": 1, "writerNickname": "홍길동",
  "writeDate": "2026-09-30T14:00:00", "updateDate": null, "viewCount": 1,
  "allowCopy": true, "copyCount": 0 }
```

**목록 `GET /api/share-pages`** 🔓: 최신 작성순으로 반환합니다.

```json
[ { "id": 1, "title": "제주도 3박 4일", "writerId": 1, "writerNickname": "홍길동",
    "writeDate": "2026-09-30T14:00:00", "viewCount": 1, "allowCopy": true, "copyCount": 0 } ]
```

**삭제 `DELETE /api/share-pages/{id}`**: 게시글에 달린 댓글도 함께 삭제됩니다.

### S2 댓글

**생성 `POST /api/share-pages/{sharePageId}/comments`**

| 필드 | 타입 | 필수 | 검증 |
|------|------|:----:|------|
| content | string | ✅ | 500자 이내 |

```json
// 201
{ "id": 1, "content": "좋은 정보 감사합니다", "writerId": 2, "writerNickname": "김철수",
  "createdAt": "2026-09-30T14:10:00" }
```

**목록 `GET /api/share-pages/{sharePageId}/comments`** 🔓: 등록순으로 반환합니다.

**삭제 `DELETE /api/comments/{commentId}`**: 작성자만 가능합니다.

### S4 공유된 여행 상세 — `GET /api/share-pages/{id}/trip` 🔓

게시글에 연결된 여행의 방문지 · 이동 구간 · 숙소 · 경비를 보여줍니다. 조회수는 오르지 않습니다.
예약번호(`reservationNo`)는 개인 정보라 응답에 넣지 않습니다 (B12).

- `stops`는 방문 순서(`stopOrder`)대로 정렬됩니다.
- 이동 구간은 `fromStopId` · `toStopId`로 `stops`의 `id`를 가리킵니다.

```json
{ "tripId": 1, "title": "제주도 여행", "startDate": "2026-10-01", "endDate": "2026-10-04",
  "stops": [
    { "id": 1, "name": "제주공항", "date": "2026-10-01", "time": "10:00:00",
      "memo": null, "imageUrl": null, "stopOrder": 1 },
    { "id": 2, "name": "제주 호텔", "date": "2026-10-01", "time": "15:00:00",
      "memo": null, "imageUrl": null, "stopOrder": 2 } ],
  "transportSegments": [
    { "id": 1, "fromStopId": 1, "toStopId": 2, "mode": "CAR",
      "departTime": "2026-10-01T10:30:00", "arriveTime": "2026-10-01T11:30:00", "cost": 20000 } ],
  "lodgings": [
    { "id": 1, "name": "제주 호텔", "checkIn": "2026-10-01T15:00:00",
      "checkOut": "2026-10-02T11:00:00", "cost": 100000 } ],
  "transportCost": 20000, "lodgingCost": 100000, "totalCost": 120000 }
```

### S5 공유된 여행 복사 — `POST /api/share-pages/{id}/copy` 🔒

게시글에 연결된 여행(방문지 · 이동 구간 · 숙소 포함)을 로그인한 사용자의 새 여행으로 복사합니다.
복사를 허용하지 않은(`allowCopy: false`) 게시글은 작성자 본인만 복사할 수 있고, 다른 사람은 403입니다 (B11).

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| startDate | date | | 복사한 여행의 시작일. 원본 시작일과의 차이만큼 모든 날짜가 같이 이동. 안 보내면 원본 날짜 그대로 |

요청 본문은 통째로 생략할 수 있습니다.

- 비용은 그대로 복사하고, 예약번호는 가져오지 않습니다 (B12).
- 이동 구간의 출발 · 도착지는 새로 복사된 방문지로 연결됩니다.
- 복사할 때마다 게시글의 `copyCount`가 1 오릅니다.
- 복사한 여행은 원본과 별개라서, 원본을 수정 · 삭제해도 영향이 없습니다.

```json
// 요청 (선택)
{ "startDate": "2026-12-01" }

// 201: 새로 만들어진 내 여행 (이후 /api/trips/{id}로 조회 · 수정)
{ "id": 7, "title": "제주도 여행", "startDate": "2026-12-01", "endDate": "2026-12-04" }
```

---

## 여행 성향 테스트 (P)

### P1 테스트 제출 — `POST /api/travel-test` 🔒

| 필드 | 타입 | 필수 | 검증 |
|------|------|:----:|------|
| selectedTypes | string[] | ✅ | 1개 이상, 빈 값(null) 없이 |

선택지 값: `FREE_EXPLORER` · `RELAXED_HEALER` · `CULTURE_LOVER` · `FOOD_EXPLORER`

가장 많이 고른 유형이 결과가 되고, 동점이면 마지막에 고른 쪽이 우선입니다. 결과는 저장됩니다.

```json
// 200
{ "travelType": "FOOD_EXPLORER", "displayName": "미식 탐구가", "description": "현지 맛집과 시장 탐방을 즐기는 유형" }
```

### P2 내 최근 결과 — `GET /api/travel-test/me` 🔒

```json
// 200
{ "travelType": "FOOD_EXPLORER", "displayName": "미식 탐구가",
  "description": "현지 맛집과 시장 탐방을 즐기는 유형", "testedAt": "2026-09-30T14:00:00" }
```
- 404: 아직 테스트한 적이 없음
