import ComingSoon from "../../components/common/ComingSoon";

export default function SharePageListPage() {
	return (
		<ComingSoon
			title="여행 계획 공유"
			figma="📄 게시판 - 목록 (김형준)"
			apis={[
				"GET /api/share-pages?page=0&size=10",
			]}
		/>
	);
}
