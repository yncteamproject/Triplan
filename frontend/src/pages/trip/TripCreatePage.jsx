import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { getErrorMessage } from "../../api/client";
import { createTrip } from "../../api/tripApi";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import styles from "./TripCreatePage.module.css";

const TOTAL_STEPS = 2;
const TITLE_MAX_LENGTH = 50; // 화면에서만 두는 제한 (서버는 길이를 검사하지 않음)

// 여행 만들기 마법사: 1단계 이름 → 2단계 기간 (피그마 "📄 Planner 페이지 - 설정 마법사 1번 · 2번")
export default function TripCreatePage() {
	const navigate = useNavigate();
	const [step, setStep] = useState(1);
	const [title, setTitle] = useState("");
	const [startDate, setStartDate] = useState("");
	const [endDate, setEndDate] = useState("");
	const [error, setError] = useState("");
	const [submitting, setSubmitting] = useState(false);

	const goNext = () => {
		if (!title.trim()) {
			setError("여행 이름을 입력해주세요");
			return;
		}
		setError("");
		setStep(2);
	};

	const goPrev = () => {
		setError("");
		setStep(1);
	};

	const create = async () => {
		if (!startDate || !endDate) {
			setError("시작일과 종료일을 선택해주세요");
			return;
		}
		// 서버도 같은 검사를 한다 (B5). 화면에서 먼저 막아서 바로 알려준다
		if (endDate < startDate) {
			setError("종료일은 시작일보다 빠를 수 없습니다");
			return;
		}
		setError("");
		setSubmitting(true);
		try {
			const trip = await createTrip({ title: title.trim(), startDate, endDate });
			navigate(`/trips/${trip.id}`, { replace: true });
		} catch (err) {
			setError(getErrorMessage(err, "여행을 만들지 못했어요. 잠시 후 다시 시도해주세요."));
			setSubmitting(false);
		}
	};

	const handleSubmit = (event) => {
		event.preventDefault();
		if (step === 1) {
			goNext();
		} else {
			create();
		}
	};

	return (
		<section className={styles.page}>
			<form className={styles.card} onSubmit={handleSubmit} noValidate>
				<div className={styles.body}>
					<p className={styles.step}>
						STEP {step} / {TOTAL_STEPS}
					</p>
					<div className={styles.dots} aria-hidden="true">
						{Array.from({ length: TOTAL_STEPS }, (_, index) => (
							<span key={index} className={index < step ? `${styles.dot} ${styles.dotOn}` : styles.dot} />
						))}
					</div>

					{step === 1 ? (
						<>
							<h2 className={styles.title}>여행 이름을 지어주세요</h2>
							<Input
								variant="box"
								label="여행 이름"
								placeholder="예: 제주도 여행"
								maxLength={TITLE_MAX_LENGTH}
								autoFocus
								value={title}
								onChange={(event) => {
									setTitle(event.target.value);
									setError("");
								}}
								error={error}
							/>
						</>
					) : (
						<>
							<h2 className={styles.title}>여행 기간을 선택하세요</h2>
							<div className={styles.dates}>
								<Input
									variant="box"
									type="date"
									label="시작일"
									value={startDate}
									onChange={(event) => {
										setStartDate(event.target.value);
										setError("");
									}}
								/>
								<Input
									variant="box"
									type="date"
									label="종료일"
									min={startDate || undefined}
									value={endDate}
									onChange={(event) => {
										setEndDate(event.target.value);
										setError("");
									}}
								/>
							</div>
							{error && <p className={styles.error}>{error}</p>}
						</>
					)}
				</div>

				<div className={styles.actions}>
					{step === 1 ? (
						<Button variant="secondary" to="/trips">
							취소
						</Button>
					) : (
						<Button variant="secondary" onClick={goPrev} disabled={submitting}>
							이전
						</Button>
					)}
					<Button type="submit" disabled={submitting}>
						{step === 1 ? "다음" : submitting ? "만드는 중…" : "플래너 만들기"}
					</Button>
				</div>
			</form>
		</section>
	);
}
