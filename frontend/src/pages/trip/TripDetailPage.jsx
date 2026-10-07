import ComingSoon from "../../components/common/ComingSoon";

export default function TripDetailPage() {
	return (
		<ComingSoon
			title="여행 상세 · 편집"
			figma="📄 플래너 - 여행 상세 편집 (김형준)"
			apis={[
				"GET /api/trips/{id}",
				"GET /api/trips/{id}/stops",
				"GET /api/trips/{id}/segments",
				"GET /api/trips/{id}/lodgings",
				"GET /api/trips/{id}/estimate",
				"GET /api/trips/{id}/transit-routes",
			]}
		/>
	);
}
