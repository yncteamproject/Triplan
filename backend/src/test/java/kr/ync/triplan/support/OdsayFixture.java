package kr.ync.triplan.support;

// 테스트용 오디세이 응답 (2026-10-06 실제 호출 결과 서울역 → 강남역을 줄여서 사용)
public final class OdsayFixture {

	private OdsayFixture() {
	}

	// 경로 4개: 일부러 소요시간순이 아니게 섞어 둠 (33분 지하철 2번, 41분 버스+버스, 36분 버스 1번, 39분 버스+지하철)
	public static final String SUCCESS_JSON = """
			{ "result": { "searchType": 0, "path": [
			  { "pathType": 1,
			    "info": { "totalTime": 33, "payment": 1650, "busTransitCount": 0, "subwayTransitCount": 2, "totalWalk": 227 },
			    "subPath": [
			      { "trafficType": 3, "distance": 221, "sectionTime": 3 },
			      { "trafficType": 1, "sectionTime": 16, "startName": "서울역", "endName": "사당",
			        "lane": [ { "name": "수도권 4호선", "subwayCode": 4 } ] },
			      { "trafficType": 3, "distance": 0, "sectionTime": 2 },
			      { "trafficType": 1, "sectionTime": 9, "startName": "사당", "endName": "강남",
			        "lane": [ { "name": "수도권 2호선", "subwayCode": 2 } ] },
			      { "trafficType": 3, "distance": 6, "sectionTime": 1 } ] },
			  { "pathType": 2,
			    "info": { "totalTime": 41, "payment": 1500, "busTransitCount": 2, "subwayTransitCount": 0, "totalWalk": 428 },
			    "subPath": [] },
			  { "pathType": 2,
			    "info": { "totalTime": 36, "payment": 2500, "busTransitCount": 1, "subwayTransitCount": 0, "totalWalk": 274 },
			    "subPath": [
			      { "trafficType": 2, "sectionTime": 34, "startName": "서울역버스환승센터(5번승강장)", "endName": "신분당선강남역",
			        "lane": [ { "busNo": "402", "type": 11 }, { "busNo": "405", "type": 11 } ] } ] },
			  { "pathType": 3,
			    "info": { "totalTime": 39, "payment": 1650, "busTransitCount": 1, "subwayTransitCount": 1, "totalWalk": 680 },
			    "subPath": [] } ] } }
			""";

	// 출발 · 도착지가 700m 이내일 때 (실제 응답 그대로)
	public static final String TOO_CLOSE_JSON = """
			{"error":{"msg":"출, 도착지가 700m이내입니다.","code":"-98"}}
			""";

	// 키 인증 실패 (실제 응답 그대로, 오류가 배열 모양)
	public static final String AUTH_FAILED_JSON = """
			{"error":[{"code":"500","message":"[ApiKeyAuthFailed] ApiKey authentication failed."}]}
			""";
}
