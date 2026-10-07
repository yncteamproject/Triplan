import axios from "axios";

const TOKEN_KEY = "accessToken";

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (token) => localStorage.setItem(TOKEN_KEY, token);
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);

// 로그인이 만료됐을 때 AuthContext에 알리는 이벤트 이름
export const AUTH_EXPIRED_EVENT = "auth:expired";

export const apiClient = axios.create({
	baseURL: import.meta.env.VITE_API_BASE_URL,
	headers: {
		"Content-Type": "application/json",
	},
});

// 로그인 후 저장해둔 토큰을 모든 요청에 자동으로 붙임
apiClient.interceptors.request.use((config) => {
	const token = getToken();
	if (token) {
		config.headers.Authorization = `Bearer ${token}`;
	}
	return config;
});

// 토큰을 붙여 보낸 요청이 401이면 로그인이 만료된 것 → 토큰을 지우고 알림
// (로그인 실패도 401이지만 토큰 없이 보내는 요청이라 여기에 걸리지 않는다)
apiClient.interceptors.response.use(
	(response) => response,
	(error) => {
		const sentWithToken = Boolean(error.config?.headers?.Authorization);
		if (error.response?.status === 401 && sentWithToken) {
			clearToken();
			window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
		}
		return Promise.reject(error);
	},
);

// 서버가 보낸 오류 메시지(ErrorResponse.message)를 꺼낸다. 없으면 기본 문구
export const getErrorMessage = (error, fallback = "요청을 처리하지 못했어요. 잠시 후 다시 시도해주세요.") =>
	error.response?.data?.message ?? fallback;
