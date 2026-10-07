import ComingSoon from "../../components/common/ComingSoon";

export default function MyPage() {
	return (
		<ComingSoon
			title="마이페이지"
			figma="📄 마이페이지 - 새 버전 (로그인 페이지 스타일)"
			apis={[
				"GET /api/members/me",
				"PUT /api/members/me",
				"GET /api/travel-test/me",
			]}
		/>
	);
}
