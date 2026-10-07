import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import * as authApi from "../api/authApi";
import { AUTH_EXPIRED_EVENT, clearToken, getToken, setToken } from "../api/client";
import { useToast } from "./ToastContext";

const USER_KEY = "user";
const AuthContext = createContext(null);

// 새로고침해도 로그인이 유지되도록 저장해둔 사용자 정보를 읽는다
function readStoredUser() {
	if (!getToken()) {
		return null;
	}
	try {
		return JSON.parse(localStorage.getItem(USER_KEY));
	} catch {
		return null;
	}
}

// 로그인 상태(토큰 · 닉네임)를 앱 전체에서 쓰게 해준다
export function AuthProvider({ children }) {
	const [user, setUser] = useState(readStoredUser);
	const { showToast } = useToast();
	const navigate = useNavigate();
	const location = useLocation();

	const login = useCallback(async (request) => {
		const { accessToken, memberId, nickname } = await authApi.login(request);
		const nextUser = { memberId, nickname };
		setToken(accessToken);
		localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
		setUser(nextUser);
		return nextUser;
	}, []);

	const logout = useCallback(() => {
		clearToken();
		localStorage.removeItem(USER_KEY);
		setUser(null);
	}, []);

	// 헤더에 보이는 닉네임을 바꿀 때 (마이페이지에서 닉네임 수정 후)
	const updateNickname = useCallback((nickname) => {
		setUser((prev) => {
			if (!prev) {
				return prev;
			}
			const nextUser = { ...prev, nickname };
			localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
			return nextUser;
		});
	}, []);

	// API가 401을 주면(client.js) 알림을 띄우고 로그아웃한 뒤 로그인 화면으로 보낸다. 로그인하면 보던 화면으로 돌아온다
	useEffect(() => {
		const handleExpired = () => {
			showToast("로그인이 만료됐어요. 다시 로그인해주세요");
			logout();
			navigate("/login", { replace: true, state: { from: location } });
		};
		window.addEventListener(AUTH_EXPIRED_EVENT, handleExpired);
		return () => window.removeEventListener(AUTH_EXPIRED_EVENT, handleExpired);
	}, [logout, navigate, location, showToast]);

	const value = useMemo(
		() => ({ user, isLoggedIn: Boolean(user), login, logout, updateNickname }),
		[user, login, logout, updateNickname],
	);

	return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react/only-export-components
export function useAuth() {
	const context = useContext(AuthContext);
	if (!context) {
		throw new Error("useAuth는 AuthProvider 안에서만 쓸 수 있어요");
	}
	return context;
}
