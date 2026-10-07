import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { getErrorMessage } from "../../api/client";
import { signup } from "../../api/authApi";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import Input from "../../components/common/Input";
import { useAuth } from "../../context/AuthContext";
import styles from "./SignupPage.module.css";

// 회원가입 화면. 가입이 끝나면 로그인 화면으로 이동.
export default function SignupPage() {
	const { isLoggedIn } = useAuth();
	const navigate = useNavigate();
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");
	const [passwordConfirm, setPasswordConfirm] = useState("");
	const [nickname, setNickname] = useState("");
	const [error, setError] = useState("");
	const [submitting, setSubmitting] = useState(false);

	// 이미 로그인했으면 메인으로 이동.
	if (isLoggedIn) {
		return <Navigate to="/" replace />;
	}

	// 비밀번호 확인은 서버로 보내지 않고 화면에서만 비교.
	const passwordMismatch = passwordConfirm !== "" && password !== passwordConfirm;

	const handleSubmit = async (event) => {
		event.preventDefault();
		setError("");
		setSubmitting(true);
		try {
			await signup({ email, password, nickname });
			navigate("/login", { replace: true });
		} catch (err) {
			setError(getErrorMessage(err, "회원가입에 실패했습니다. 잠시 후 다시 시도해주세요."));
		} finally {
			setSubmitting(false);
		}
	};

	return (
		<section className={styles.wrap}>
			<Card variant="form" className={styles.card}>
				<h1 className={styles.brand}>Triplan</h1>
				<p className={styles.subtitle}>회원가입을 하여 즐거운 여행을 위한 계획을 시작해보세요</p>
				<form className={styles.form} onSubmit={handleSubmit} noValidate>
					<Input
						type="email"
						placeholder="이메일을 입력해주세요"
						autoComplete="email"
						helper="로그인할 때 아이디로 사용해요"
						value={email}
						onChange={(event) => setEmail(event.target.value)}
					/>
					<Input
						type="password"
						placeholder="비밀번호를 입력해주세요"
						autoComplete="new-password"
						helper="8~20자"
						maxLength={20}
						value={password}
						onChange={(event) => setPassword(event.target.value)}
					/>
					<Input
						type="password"
						placeholder="비밀번호를 한 번 더 입력해주세요"
						autoComplete="new-password"
						maxLength={20}
						value={passwordConfirm}
						onChange={(event) => setPasswordConfirm(event.target.value)}
						error={passwordMismatch ? "비밀번호가 일치하지 않아요" : ""}
					/>
					<Input
						placeholder="닉네임을 입력해주세요"
						autoComplete="nickname"
						helper="게시글과 댓글에 표시됩니다 (20자 이내)"
						maxLength={20}
						value={nickname}
						onChange={(event) => setNickname(event.target.value)}
						error={error}
					/>
					<Button
						type="submit"
						size="lg"
						fullWidth
						className={styles.submit}
						disabled={submitting || !email || !password || !passwordConfirm || !nickname || passwordMismatch}
					>
						{submitting ? "가입 중…" : "회원가입"}
					</Button>
				</form>
				<p className={styles.footer}>
					이미 가입된 계정이 있으신가요?
					<Link to="/login">로그인</Link>
				</p>
			</Card>
		</section>
	);
}