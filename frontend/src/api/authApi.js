import { apiClient } from "./client";

export const signup = (request) =>
	apiClient.post("/api/auth/signup", request).then((res) => res.data);

// 응답: { accessToken, memberId, nickname }. 토큰 저장은 AuthContext의 login()이 한다
export const login = (request) =>
	apiClient.post("/api/auth/login", request).then((res) => res.data);
