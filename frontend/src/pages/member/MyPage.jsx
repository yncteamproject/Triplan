import { useEffect, useState } from "react";
import { getErrorMessage } from "../../api/client";
import { getMyInfo, updateMyInfo } from "../../api/memberApi";
import { getMyTravelResult } from "../../api/travelTestApi";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import EmptyState from "../../components/common/EmptyState";
import Input from "../../components/common/Input";
import { useAuth } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import styles from "./MyPage.module.css";

const formatDate = (dateTime) => dateTime.slice(0, 10).replaceAll("-", ".");

export default function MyPage() {
	const { updateNickname } = useAuth();
	const { showToast } = useToast();
	const [me, setMe] = useState(null);
	const [loadError, setLoadError] = useState("");
	const [nickname, setNickname] = useState("");
	const [password, setPassword] = useState("");
	const [passwordConfirm, setPasswordConfirm] = useState("");
	const [error, setError] = useState("");
	const [submitting, setSubmitting] = useState(false);

	useEffect(() => {
		getMyInfo()
			.then(setMe)
			.catch((err) => setLoadError(getErrorMessage(err, "내 정보를 불러오지 못했어요.")));
	}, []);

	const passwordMismatch = password !== passwordConfirm;
	const nothingToSave = nickname.trim() === "" && password === "";

	const handleSubmit = async (event) => {
		event.preventDefault();
		setError("");
		setSubmitting(true);
		try {
			// 빈칸은 서버에서 기존 값 유지
			const updated = await updateMyInfo({ nickname, password });
			setMe(updated);
			updateNickname(updated.nickname);
			setNickname("");
			setPassword("");
			setPasswordConfirm("");
			showToast("내 정보를 저장했어요");
		} catch (err) {
			setError(getErrorMessage(err, "저장하지 못했어요. 잠시 후 다시 시도해주세요."));
		} finally {
			setSubmitting(false);
		}
	};

	return (
		<section className={styles.wrap}>
			<div className={styles.inner}>
				<div>
					<h1 className={styles.title}>마이페이지</h1>
				</div>

				<Card variant="form">
					{me ? (
						<div className={styles.profile}>
							<div className={styles.avatar}>{me.nickname.charAt(0)}</div>
							<div>
								<p className={styles.nickname}>{me.nickname}</p>
								<p className={styles.email}>{me.email}</p>
								<p className={styles.joined}>가입일 {formatDate(me.createdAt)}</p>
							</div>
						</div>
					) : (
						<p className={styles.status}>{loadError || "불러오는 중"}</p>
					)}
				</Card>

				<Card variant="form">
					<h2 className={styles.cardTitle}>내 정보 수정</h2>
					<p className={styles.cardDescription}>비워 둔 항목은 바뀌지 않아요</p>
					<form className={styles.form} onSubmit={handleSubmit} noValidate>
						<Input
							label="닉네임"
							placeholder="새 닉네임을 입력해주세요 (20자 이내)"
							autoComplete="nickname"
							maxLength={20}
							value={nickname}
							onChange={(event) => setNickname(event.target.value)}
						/>
						<Input
							label="새 비밀번호"
							type="password"
							placeholder="변경할 때만 입력해주세요 (8~20자)"
							autoComplete="new-password"
							maxLength={20}
							value={password}
							onChange={(event) => setPassword(event.target.value)}
						/>
						<Input
							label="새 비밀번호 확인"
							type="password"
							placeholder="한 번 더 입력해주세요"
							autoComplete="new-password"
							maxLength={20}
							value={passwordConfirm}
							onChange={(event) => setPasswordConfirm(event.target.value)}
							error={passwordConfirm && passwordMismatch ? "비밀번호가 일치하지 않아요" : error}
						/>
						<Button
							type="submit"
							size="lg"
							fullWidth
							disabled={submitting || nothingToSave || passwordMismatch}
						>
							{submitting ? "저장 중" : "저장하기"}
						</Button>
					</form>
				</Card>
				<TravelResultCard />
			</div>
		</section>
	);
}

function TravelResultCard() {
	const [result, setResult] = useState(undefined);
	const [loadError, setLoadError] = useState("");

	useEffect(() => {
		getMyTravelResult()
			.then(setResult)
			.catch((err) => {
				// 테스트를 안 했으면 404
				if (err.response?.status === 404) {
					setResult(null);
				} else {
					setLoadError(getErrorMessage(err, "여행 성향을 불러오지 못했어요."));
				}
			});
	}, []);

	if (result === null) {
		return (
			<Card variant="form">
				<h2 className={styles.cardTitle}>나의 여행 성향</h2>
				<p className={styles.cardDescription}>아직 결과가 없어요</p>
				<div className={styles.cardBody}>
					<EmptyState
						title="아직 여행 성향 테스트를 하지 않았어요"
						description="10개 질문으로 나에게 맞는 여행 스타일을 알아보세요"
						action={<Button to="/travel-test">테스트 하러 가기</Button>}
					/>
				</div>
			</Card>
		);
	}

	return (
		<Card variant="form">
			<h2 className={styles.cardTitle}>나의 여행 성향</h2>
			<p className={styles.cardDescription}>가장 최근에 한 테스트 결과예요</p>
			<div className={styles.cardBody}>
				{result ? (
					<>
						<span className={styles.typeBadge}>{result.displayName}</span>
						<p className={styles.typeDescription}>{result.description}</p>
						<p className={styles.joined}>테스트한 날 {formatDate(result.testedAt)}</p>
						<div className={styles.links}>
							<Button variant="text" to="/travel-test/result">결과 자세히 보기</Button>
							<Button variant="text" to="/travel-test">테스트 다시 하기</Button>
						</div>
					</>
				) : (
					<p className={styles.status}>{loadError || "불러오는 중"}</p>
				)}
			</div>
		</Card>
	);
}