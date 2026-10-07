import ComingSoon from "../../components/common/ComingSoon";

export default function TravelTestResultPage() {
	return (
		<ComingSoon
			title="여행 성향 테스트 결과"
			figma="📄 여행 성향 테스트 결과(윤효근)"
			apis={[
				"GET /api/travel-test/me",
			]}
		/>
	);
}
