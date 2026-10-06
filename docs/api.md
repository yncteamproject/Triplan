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
| T7 | GET | `/api/trips/{tripId}/transit-routes` | 🔒 본인 | 200 |
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
| latitude | number | | 위도, -90 ~ 90. 경도와 함께 보내야 함 |
| longitude | number | | 경도, -180 ~ 180. 위도와 함께 보내야 함 |
| address | string | | 255자 이내 |

```json
// 201 / 200
{ "id": 1, "tripId": 1, "name": "성산일출봉", "date": "2026-10-01", "time": "09:00:00",
  "memo": "일출 명소", "imageUrl": null, "stopOrder": 1,
  "latitude": 33.4581, "longitude": 126.9425, "address": "제주 서귀포시 성산읍" }
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

### T7 대중교통 경로 — `GET /api/trips/{tripId}/transit-routes?fromStopId=1&toStopId=2`

두 방문지의 위도 · 경도로 오디세이(ODsay) 대중교통 길찾기를 조회해서, 경로 후보를 **소요시간이 짧은 순으로 최대 3개** 반환합니다.
오디세이 키는 서버에만 있고(`application-secret.yaml`), 프론트는 이 API만 호출합니다. 오디세이 쪽 세부 내용(파라미터 · 오류 모양)은 [외부 API — 오디세이](#오디세이odsay-대중교통-길찾기--t7)에 있습니다.

| 파라미터 | 필수 | 설명 |
|----------|:----:|------|
| fromStopId | ✅ | 출발 방문지. 이 여행의 방문지여야 함 |
| toStopId | ✅ | 도착 방문지. 이 여행의 방문지여야 함 |

```json
{ "routes": [
    { "totalTime": 33, "payment": 1650, "transferCount": 1, "walkDistance": 227,
      "steps": [
        { "type": "WALK", "name": null, "from": null, "to": null, "time": 3 },
        { "type": "SUBWAY", "name": "수도권 4호선", "from": "서울역", "to": "사당", "time": 16 },
        { "type": "WALK", "name": null, "from": null, "to": null, "time": 2 },
        { "type": "SUBWAY", "name": "수도권 2호선", "from": "사당", "to": "강남", "time": 9 },
        { "type": "WALK", "name": null, "from": null, "to": null, "time": 1 } ] } ] }
```

| 응답 필드 | 설명 |
|-----------|------|
| totalTime | 총 소요시간 (분) |
| payment | 요금 (원) |
| transferCount | 환승 횟수 |
| walkDistance | 총 도보 거리 (m) |
| steps[].type | `SUBWAY` 지하철 / `BUS` 버스 / `WALK` 도보 |
| steps[].name | 지하철 노선명 또는 버스 번호 (탈 수 있는 버스가 여러 대면 `"402, 405"`). 도보는 `null` |
| steps[].from · to | 타는 곳 · 내리는 곳. 도보는 `null` |
| steps[].time | 구간 소요시간 (분) |

- 경로가 없으면 (출발 · 도착이 700m 이내이거나 대중교통이 없음) 빈 `routes`와 200.
- 방문지에 위도 · 경도가 없으면 400, 출발과 도착이 같으면 400.
- 다른 여행의 방문지를 넣으면 403 (B10과 같은 방식), 남의 여행이면 403 (B2).
- 오디세이 호출이 실패하면 (키 없음 · 키 오류 · 서버 장애 · 시간 초과 · 하루 호출 횟수 초과) 502 "대중교통 정보를 불러오지 못했습니다."
- 같은 출발 · 도착 좌표의 결과는 서버 메모리에 저장해 두고 재사용합니다 (오디세이 무료 호출 하루 30건 절약). 서버를 다시 시작하면 지워지고, 실패한 조회는 저장하지 않습니다.

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
      "memo": null, "imageUrl": null, "stopOrder": 1,
      "latitude": 33.5059, "longitude": 126.4959, "address": "제주 제주시 공항로 2" },
    { "id": 2, "name": "제주 호텔", "date": "2026-10-01", "time": "15:00:00",
      "memo": null, "imageUrl": null, "stopOrder": 2,
      "latitude": null, "longitude": null, "address": null } ],
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

---

## 외부 API

우리 서버가 호출하는 외부 API입니다. 공식 문서를 옮겨 적지 않고, **우리가 쓰는 부분과 직접 호출해서 알아낸 점**만 적습니다.
키는 모두 `application-secret.yaml`에 두고 git에 올리지 않습니다.

### 오디세이(ODsay) 대중교통 길찾기 — T7

| 항목 | 내용 |
|------|------|
| 공식 문서 | [ODsay LAB](https://lab.odsay.com) |
| 쓰는 API | `GET https://api.odsay.com/v1/api/searchPubTransPathT` |
| 설정 | `odsay.api-key` (없으면 서버는 뜨고 경로 조회만 502) |
| 코드 | `trip/client/OdsayClient`(호출), `OdsayResponseParser`(해석), `trip/service/TransitRouteCache`(캐시) |
| 키 발급 | 애플리케이션을 **Server** 플랫폼으로 등록하고, 호출할 컴퓨터의 공인 IP를 등록 (`curl.exe ifconfig.me`). IP는 여러 개 등록 가능 |
| 사용량 | 무료 하루 30건 (키마다 따로). 그래서 각자 키를 발급받고, 같은 경로는 캐시로 재사용 |
| 응답 시간 | 첫 조회 4~5초 (2026-10-06 실측). 응답 대기 제한은 10초 |

**요청 파라미터**

| 파라미터 | 내용 |
|----------|------|
| `SX`, `SY` | 출발지 **경도**, **위도** |
| `EX`, `EY` | 도착지 **경도**, **위도** |
| `apiKey` | 발급받은 키. `+`, `/`, `=`가 들어 있을 수 있어 반드시 URL 인코딩 |

> [!WARNING]
> X가 경도, Y가 위도입니다. 우리 방문지는 `latitude`(위도) · `longitude`(경도)로 저장하므로 순서를 바꿔서 넘깁니다.

**성공 응답에서 쓰는 값** (`result.path[]`가 경로 후보)

| 오디세이 필드 | 우리 응답 | 주의 |
|---------------|-----------|------|
| `info.totalTime` | `totalTime` | 분. **후보가 소요시간순으로 오지 않아서** 직접 정렬 |
| `info.payment` | `payment` | 원 |
| `info.busTransitCount` + `info.subwayTransitCount` | `transferCount` | 두 값은 **탄 횟수**라서 환승 횟수는 (합 - 1) |
| `info.totalWalk` | `walkDistance` | m |
| `subPath[].trafficType` | `steps[].type` | 1 지하철 · 2 버스 · 3 도보 |
| `subPath[].lane[].name` / `busNo` | `steps[].name` | 지하철은 `name`(예: 수도권 4호선), 버스는 `busNo`. 여러 개면 쉼표로 연결 |
| `subPath[].startName` · `endName` · `sectionTime` | `from` · `to` · `time` | 도보 구간에는 이름이 없음 |

**실패 응답** — HTTP 상태는 **실패해도 200**이고, 본문의 `error`로 구분합니다. 모양이 두 가지입니다.

| 경우 | 실제 응답 | 우리 처리 |
|------|-----------|-----------|
| 출발 · 도착이 700m 이내 | `{"error":{"msg":"출, 도착지가 700m이내입니다.","code":"-98"}}` | 빈 목록, 200 |
| 검색 결과 없음 | `{"error":{"code":"-99", ...}}` (문서 기준, 직접 확인 전) | 빈 목록, 200 |
| 키 인증 실패 | `{"error":[{"code":"500","message":"[ApiKeyAuthFailed] ApiKey authentication failed."}]}` (**배열**) | 502 |
| 그 외 오류 · 시간 초과 · JSON이 아님 | — | 502 |

하루 호출 횟수를 넘었을 때의 응답은 아직 확인하지 못했습니다. 지금은 "그 외 오류"로 502를 반환합니다. 확인되면 이 표에 추가합니다.
