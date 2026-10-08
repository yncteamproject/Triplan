import { apiClient } from "./client";

// 로그인한 사용자의 여행 목록 (사용자는 토큰으로 구분)
export const getTrips = () =>
	apiClient.get("/api/trips").then((res) => res.data);

export const getTrip = (id) =>
	apiClient.get(`/api/trips/${id}`).then((res) => res.data);

export const createTrip = (request) =>
	apiClient.post("/api/trips", request).then((res) => res.data);

export const updateTrip = (id, request) =>
	apiClient.put(`/api/trips/${id}`, request).then((res) => res.data);

// 방문지 · 이동 구간 · 숙소와, 이 여행을 공유한 게시글도 함께 지워진다
export const deleteTrip = (id) => apiClient.delete(`/api/trips/${id}`);

// 방문지는 서버가 방문 순서대로 준다
export const getStops = (tripId) =>
	apiClient.get(`/api/trips/${tripId}/stops`).then((res) => res.data);

export const getTransportSegments = (tripId) =>
	apiClient.get(`/api/trips/${tripId}/transport-segments`).then((res) => res.data);

export const getLodgings = (tripId) =>
	apiClient.get(`/api/trips/${tripId}/lodgings`).then((res) => res.data);

// 견적: 교통비 · 숙박비 · 합계
export const getEstimate = (tripId) =>
	apiClient.get(`/api/trips/${tripId}/estimate`).then((res) => res.data);
