import { apiClient } from "./client";

export const signup = (request) =>
	apiClient.post("/api/auth/signup", request).then((res) => res.data);

// 로그인 성공 시 토큰을 저장 → 이후 요청에 자동으로 포함됨
export const login = (request) =>
	apiClient.post("/api/auth/login", request).then((res) => {
		localStorage.setItem("accessToken", res.data.accessToken);
		return res.data;
	});

export const logout = () => localStorage.removeItem("accessToken");
