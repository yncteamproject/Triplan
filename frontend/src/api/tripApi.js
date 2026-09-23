import { apiClient } from "./client";

// 로그인한 사용자의 여행 목록 (사용자는 토큰으로 구분)
export const getTrips = () =>
	apiClient.get("/api/trips").then((res) => res.data);

export const getTrip = (id) =>
	apiClient.get(`/api/trips/${id}`).then((res) => res.data);

export const createTrip = (request) =>
	apiClient.post("/api/trips", request).then((res) => res.data);
