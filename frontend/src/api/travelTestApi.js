import { apiClient } from "./client";

// 테스트를 안 했다면 404
export const getMyTravelResult = () =>
	apiClient.get("/api/travel-test/me").then((res) => res.data);
