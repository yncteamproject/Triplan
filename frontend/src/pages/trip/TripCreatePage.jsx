import ComingSoon from "../../components/common/ComingSoon";

export default function TripCreatePage() {
	return (
		<ComingSoon
			title="여행 만들기"
			figma="📄 Planner 페이지 - 설정 마법사 1번 · 2번"
			apis={[
				"POST /api/trips",
			]}
		/>
	);
}
