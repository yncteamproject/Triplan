import { apiClient } from "./client";

export type TravelType = "FREE_EXPLORER" | "RELAXED_HEALER" | "CULTURE_LOVER" | "FOOD_EXPLORER";

// 서버 응답. testedAt은 내 결과 조회에만 온다
export interface TravelResult {
	travelType: TravelType;
	displayName: string;
	description: string;
	testedAt?: string;
}

// 테스트를 안 했다면 404
export const getMyTravelResult = () =>
	apiClient.get<TravelResult>("/api/travel-test/me").then((res) => res.data);

// 고른 성향 목록을 보내면 결과를 계산해서 저장한다
export const submitTravelTest = (selectedTypes: TravelType[]) =>
	apiClient.post<TravelResult>("/api/travel-test", { selectedTypes }).then((res) => res.data);