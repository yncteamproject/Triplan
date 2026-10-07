import { apiClient } from "./client";

// 내 정보.
export const getMyInfo = () =>
	apiClient.get("/api/members/me").then((res) => res.data);

// 바꿀 항목만 보낸다. 빈칸은 서버가 "안 바꿈"으로 처리.
export const updateMyInfo = (request) =>
	apiClient.put("/api/members/me", request).then((res) => res.data);
