import ComingSoon from "../../components/common/ComingSoon";

export default function TravelTestPage() {
	return (
		<ComingSoon
			title="여행 성향 테스트"
			figma="📄 여행 성향 테스트(윤효근)"
			apis={[
				"POST /api/travel-test",
			]}
		/>
	);
}
