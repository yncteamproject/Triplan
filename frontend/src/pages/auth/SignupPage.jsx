import ComingSoon from "../../components/common/ComingSoon";

export default function SignupPage() {
	return (
		<ComingSoon
			title="회원가입"
			figma="📄 회원가입 페이지(김형준)"
			apis={[
				"POST /api/auth/signup",
			]}
		/>
	);
}
