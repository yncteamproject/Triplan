import ComingSoon from "../components/common/ComingSoon";

export default function MainPage() {
	return (
		<ComingSoon
			title="메인"
			figma="메인페이지(김형준)"
			apis={[
				"GET /api/share-pages?page=0&size=3",
			]}
		/>
	);
}
