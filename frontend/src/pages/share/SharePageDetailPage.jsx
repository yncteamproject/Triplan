import ComingSoon from "../../components/common/ComingSoon";

export default function SharePageDetailPage() {
	return (
		<ComingSoon
			title="공유 게시글"
			figma="📄 게시판 - 상세 및 댓글 (김형준)"
			apis={[
				"GET /api/share-pages/{id}",
				"GET /api/share-pages/{id}/trip",
				"GET /api/share-pages/{id}/comments",
				"POST /api/share-pages/{id}/copy",
			]}
		/>
	);
}
