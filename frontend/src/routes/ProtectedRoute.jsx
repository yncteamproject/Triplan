import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

// 로그인이 필요한 화면을 감싼다. 로그인 전이면 로그인 화면으로 보내고, 로그인하면 원래 가려던 화면으로 돌아온다
export default function ProtectedRoute() {
	const { isLoggedIn } = useAuth();
	const location = useLocation();

	if (!isLoggedIn) {
		return <Navigate to="/login" replace state={{ from: location }} />;
	}
	return <Outlet />;
}
