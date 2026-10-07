import Button from "../components/common/Button";
import EmptyState from "../components/common/EmptyState";

export default function NotFoundPage() {
	return (
		<section style={{ maxWidth: 480, margin: "0 auto", padding: "96px 24px" }}>
			<EmptyState
				title="페이지를 찾을 수 없어요"
				description="주소가 바뀌었거나 없는 페이지예요"
				action={<Button to="/">홈으로 가기</Button>}
			/>
		</section>
	);
}
