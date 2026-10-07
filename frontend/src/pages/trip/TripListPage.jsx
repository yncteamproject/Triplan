import ComingSoon from "../../components/common/ComingSoon";

export default function TripListPage() {
	return (
		<ComingSoon
			title="내 여행 계획"
			figma="📄 플래너 - 내 여행 목록 / planner 페이지 - 플래너 없는 경우"
			apis={[
				"GET /api/trips",
			]}
		/>
	);
}
