import ComingSoon from "../../components/common/ComingSoon";

export default function SharePageFormPage() {
	return (
		<ComingSoon
			title="여행 계획 공유하기"
			figma="📄 게시판 - 글쓰기 · 수정 (김형준)"
			apis={[
				"GET /api/trips",
				"POST /api/share-pages",
				"PUT /api/share-pages/{id}",
			]}
		/>
	);
}
