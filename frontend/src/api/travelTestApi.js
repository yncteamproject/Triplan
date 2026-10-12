import { apiClient } from "./client";

// 테스트를 안 했다면 404
export const getMyTravelResult = () =>
	apiClient.get("/api/travel-test/me").then((res) => res.data);

// 고른 성향 목록을 보내면 결과를 계산해서 저장한다. 응답: { travelType, displayName, description }
export const submitTravelTest = (selectedTypes) =>
	apiClient.post("/api/travel-test", { selectedTypes }).then((res) => res.data);