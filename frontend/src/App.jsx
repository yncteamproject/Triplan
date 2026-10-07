import { Route, Routes } from "react-router-dom";
import Layout from "./components/layout/Layout";
import MainPage from "./pages/MainPage";
import NotFoundPage from "./pages/NotFoundPage";
import LoginPage from "./pages/auth/LoginPage";
import SignupPage from "./pages/auth/SignupPage";
import MyPage from "./pages/member/MyPage";
import SharePageDetailPage from "./pages/share/SharePageDetailPage";
import SharePageFormPage from "./pages/share/SharePageFormPage";
import SharePageListPage from "./pages/share/SharePageListPage";
import TravelTestPage from "./pages/traveltest/TravelTestPage";
import TravelTestResultPage from "./pages/traveltest/TravelTestResultPage";
import TripCreatePage from "./pages/trip/TripCreatePage";
import TripDetailPage from "./pages/trip/TripDetailPage";
import TripListPage from "./pages/trip/TripListPage";
import ProtectedRoute from "./routes/ProtectedRoute";

// 화면 주소 목록. 새 화면은 여기에 등록한다 (README "화면 주소" 표도 같이 수정)
export default function App() {
	return (
		<Routes>
			<Route element={<Layout />}>
				{/* 로그인 없이 볼 수 있는 화면 */}
				<Route path="/" element={<MainPage />} />
				<Route path="/login" element={<LoginPage />} />
				<Route path="/signup" element={<SignupPage />} />
				<Route path="/share-pages" element={<SharePageListPage />} />
				<Route path="/share-pages/:id" element={<SharePageDetailPage />} />

				{/* 로그인이 필요한 화면 */}
				<Route element={<ProtectedRoute />}>
					<Route path="/trips" element={<TripListPage />} />
					<Route path="/trips/new" element={<TripCreatePage />} />
					<Route path="/trips/:tripId" element={<TripDetailPage />} />
					<Route path="/share-pages/new" element={<SharePageFormPage />} />
					<Route path="/share-pages/:id/edit" element={<SharePageFormPage />} />
					<Route path="/travel-test" element={<TravelTestPage />} />
					<Route path="/travel-test/result" element={<TravelTestResultPage />} />
					<Route path="/mypage" element={<MyPage />} />
				</Route>

				<Route path="*" element={<NotFoundPage />} />
			</Route>
		</Routes>
	);
}
