import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { getErrorMessage } from "../../api/client";
import { submitTravelTest } from "../../api/travelTestApi";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import questions from "./questions";
import styles from "./TravelTestPage.module.css";

export default function TravelTestPage() {
	const navigate = useNavigate();
	const [step, setStep] = useState(0);
	const [answers, setAnswers] = useState([]);
	const [error, setError] = useState("");
	const [submitting, setSubmitting] = useState(false);

	const question = questions[step];
	const selected = answers[step];
	const isLast = step === questions.length - 1;

	const choose = (type) => {
		const next = [...answers];
		next[step] = type;
		setAnswers(next);
	};

	const goNext = async () => {
		if (!isLast) {
			setStep(step + 1);
			return;
		}
		setError("");
		setSubmitting(true);
		try {
			await submitTravelTest(answers);
			navigate("/travel-test/result");
		} catch (err) {
			setError(getErrorMessage(err, "결과를 저장하지 못했어요. 잠시 후 다시 시도해주세요."));
			setSubmitting(false);
		}
	};

	let nextLabel = "다음";
	if (isLast) {
		nextLabel = submitting ? "결과 계산 중" : "결과 보기";
	}

	return (
		<section className={styles.wrap}>
			<Card className={styles.card}>
				<div className={styles.progress}>
					<div className={styles.bar}>
						<div className={styles.fill} style={{ width: `${((step + 1) / questions.length) * 100}%` }} />
					</div>
					<span className={styles.count}>
						{step + 1} / {questions.length}
					</span>
				</div>

				<p className={styles.number}>Q{step + 1}</p>
				<h1 className={styles.title}>{question.title}</h1>

				<div className={styles.options}>
					{question.options.map((option, index) => (
						<button
							key={option.type}
							type="button"
							className={selected === option.type ? `${styles.option} ${styles.selected}` : styles.option}
							onClick={() => choose(option.type)}
						>
							<span className={styles.mark}>{index === 0 ? "A" : "B"}</span>
							<span className={styles.label}>{option.label}</span>
						</button>
					))}
				</div>

				{error && <p className={styles.error}>{error}</p>}

				<div className={styles.actions}>
					{step > 0 && (
						<Button variant="text" onClick={() => setStep(step - 1)} disabled={submitting}>
							이전
						</Button>
					)}
					<Button size="lg" className={styles.next} onClick={goNext} disabled={!selected || submitting}>
						{nextLabel}
					</Button>
				</div>
			</Card>
		</section>
	);
}