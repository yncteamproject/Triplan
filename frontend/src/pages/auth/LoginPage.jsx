import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { getErrorMessage } from "../../api/client";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import Input from "../../components/common/Input";
import { useAuth } from "../../context/AuthContext";
import styles from "./LoginPage.module.css";

// 로그인 화면 (피그마 "📄 로그인 페이지(윤효근)"). 공통 부품을 쓰는 예시이기도 하다
export default function LoginPage() {
	const { isLoggedIn, login } = useAuth();
	const navigate = useNavigate();
	const location = useLocation();
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");
	const [error, setError] = useState("");
	const [submitting, setSubmitting] = useState(false);

	// 로그인이 필요한 화면에서 왔으면 로그인 후 그 화면으로 돌아간다
	const from = location.state?.from?.pathname ?? "/";

	if (isLoggedIn) {
		return <Navigate to={from} replace />;
	}

	const handleSubmit = async (event) => {
		event.preventDefault();
		setError("");
		setSubmitting(true);
		try {
			await login({ email, password });
			navigate(from, { replace: true });
		} catch (err) {
			setError(getErrorMessage(err, "로그인하지 못했어요. 잠시 후 다시 시도해주세요."));
		} finally {
			setSubmitting(false);
		}
	};

	return (
		<section className={styles.wrap}>
			<Card variant="form" className={styles.card}>
				<h1 className={styles.brand}>Triplan</h1>
				<form className={styles.form} onSubmit={handleSubmit} noValidate>
					<Input
						type="email"
						placeholder="이메일을 입력해주세요"
						autoComplete="email"
						value={email}
						onChange={(event) => setEmail(event.target.value)}
					/>
					<Input
						type="password"
						placeholder="비밀번호를 입력해주세요"
						autoComplete="current-password"
						value={password}
						onChange={(event) => setPassword(event.target.value)}
						error={error}
					/>
					<Button type="submit" size="lg" fullWidth disabled={submitting || !email || !password}>
						{submitting ? "로그인 중…" : "로그인"}
					</Button>
				</form>
				<Link to="/signup" className={styles.signup}>
					회원가입
				</Link>
			</Card>
		</section>
	);
}
