import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import Toast from "../components/common/Toast";

const ToastContext = createContext(null);
const DURATION_MS = 3000;

// 화면 위쪽에 잠깐 뜨는 알림. 어디서든 useToast().showToast("문구")로 띄운다
export function ToastProvider({ children }) {
	const [message, setMessage] = useState(null);

	const showToast = useCallback((text) => setMessage(text), []);

	useEffect(() => {
		if (!message) {
			return undefined;
		}
		const timer = setTimeout(() => setMessage(null), DURATION_MS);
		return () => clearTimeout(timer);
	}, [message]);

	const value = useMemo(() => ({ showToast }), [showToast]);

	return (
		<ToastContext.Provider value={value}>
			{children}
			{message && <Toast message={message} />}
		</ToastContext.Provider>
	);
}

// eslint-disable-next-line react/only-export-components
export function useToast() {
	const context = useContext(ToastContext);
	if (!context) {
		throw new Error("useToast는 ToastProvider 안에서만 쓸 수 있어요");
	}
	return context;
}
