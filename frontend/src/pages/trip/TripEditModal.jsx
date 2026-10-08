import { useState } from "react";
import { getErrorMessage } from "../../api/client";
import { updateTrip } from "../../api/tripApi";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import Modal from "../../components/common/Modal";
import styles from "./TripEditModal.module.css";

const TITLE_MAX_LENGTH = 50; // 여행 만들기와 같은 제한 (화면에서만)

// 여행 이름 · 기간 수정 창. 저장되면 onSaved(바뀐 여행)를 부른다
export default function TripEditModal({ trip, onClose, onSaved }) {
	const [title, setTitle] = useState(trip.title);
	const [startDate, setStartDate] = useState(trip.startDate);
	const [endDate, setEndDate] = useState(trip.endDate);
	const [error, setError] = useState("");
	const [submitting, setSubmitting] = useState(false);

	const handleSubmit = async (event) => {
		event.preventDefault();
		if (!title.trim()) {
			setError("여행 이름을 입력해주세요");
			return;
		}
		if (!startDate || !endDate) {
			setError("시작일과 종료일을 선택해주세요");
			return;
		}
		// 서버도 같은 검사를 한다 (B5)
		if (endDate < startDate) {
			setError("종료일은 시작일보다 빠를 수 없습니다");
			return;
		}
		setError("");
		setSubmitting(true);
		try {
			onSaved(await updateTrip(trip.id, { title: title.trim(), startDate, endDate }));
		} catch (err) {
			setError(getErrorMessage(err, "저장하지 못했어요. 잠시 후 다시 시도해주세요."));
			setSubmitting(false);
		}
	};

	return (
		<Modal title="여행 정보 수정" onClose={onClose}>
			<form className={styles.form} onSubmit={handleSubmit} noValidate>
				<Input
					variant="box"
					label="여행 이름"
					maxLength={TITLE_MAX_LENGTH}
					autoFocus
					value={title}
					onChange={(event) => {
						setTitle(event.target.value);
						setError("");
					}}
				/>
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
				<p className={styles.helper}>기간을 줄여도 일정은 지워지지 않아요. 기간 밖 일정은 "기간 밖" 탭에 모여요.</p>
				{error && <p className={styles.error}>{error}</p>}
				<div className={styles.actions}>
					<Button variant="secondary" onClick={onClose} disabled={submitting}>
						취소
					</Button>
					<Button type="submit" disabled={submitting}>
						{submitting ? "저장 중…" : "저장"}
					</Button>
				</div>
			</form>
		</Modal>
	);
}
