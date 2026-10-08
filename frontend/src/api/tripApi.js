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

// 방문지 · 이동 구간 · 숙소의 추가 · 수정 · 삭제
// 수정(PUT)은 보낸 값으로 전부 바뀐다. 비워 보낸 칸은 지워지니 기존 값을 모두 채워서 보낸다
export const createStop = (tripId, request) =>
	apiClient.post(`/api/trips/${tripId}/stops`, request).then((res) => res.data);

export const updateStop = (id, request) =>
	apiClient.put(`/api/stops/${id}`, request).then((res) => res.data);

// 이 방문지를 출발지 · 도착지로 쓰는 이동 구간도 함께 지워진다
export const deleteStop = (id) => apiClient.delete(`/api/stops/${id}`);

export const createTransportSegment = (tripId, request) =>
	apiClient.post(`/api/trips/${tripId}/transport-segments`, request).then((res) => res.data);

export const updateTransportSegment = (id, request) =>
	apiClient.put(`/api/transport-segments/${id}`, request).then((res) => res.data);

export const deleteTransportSegment = (id) => apiClient.delete(`/api/transport-segments/${id}`);

export const createLodging = (tripId, request) =>
	apiClient.post(`/api/trips/${tripId}/lodgings`, request).then((res) => res.data);

export const updateLodging = (id, request) =>
	apiClient.put(`/api/lodgings/${id}`, request).then((res) => res.data);

export const deleteLodging = (id) => apiClient.delete(`/api/lodgings/${id}`);
