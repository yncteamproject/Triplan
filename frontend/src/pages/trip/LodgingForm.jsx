import { useState } from "react";
import { getErrorMessage } from "../../api/client";
import { createLodging, updateLodging } from "../../api/tripApi";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import { toDateTimeInput } from "../../utils/date";
import { isValidCost, toCost } from "../../utils/format";
import styles from "./TripForms.module.css";

// 숙소 추가 · 수정 폼. lodging이 있으면 수정, 없으면 추가 (defaults: 새 숙소의 기본 체크인 · 체크아웃)
export default function LodgingForm({ trip, lodging, defaults, onCancel, onSaved }) {
	const [name, setName] = useState(lodging?.name ?? "");
	const [checkIn, setCheckIn] = useState(lodging ? toDateTimeInput(lodging.checkIn) : defaults.checkIn);
	const [checkOut, setCheckOut] = useState(lodging ? toDateTimeInput(lodging.checkOut) : defaults.checkOut);
	const [cost, setCost] = useState(lodging?.cost != null ? String(lodging.cost) : "");
	const [reservationNo, setReservationNo] = useState(lodging?.reservationNo ?? "");
	const [error, setError] = useState("");
	const [submitting, setSubmitting] = useState(false);

	const handleSubmit = async (event) => {
		event.preventDefault();
		if (!name.trim()) {
			setError("숙소 이름을 입력해주세요");
			return;
		}
		if (!checkIn || !checkOut) {
			setError("체크인 시간과 체크아웃 시간을 입력해주세요");
			return;
		}
		// 서버도 같은 검사를 한다 (B6, B7)
		if (checkOut < checkIn) {
			setError("체크아웃 시간은 체크인 시간보다 빠를 수 없습니다");
			return;
		}
		if (!isValidCost(cost)) {
			setError("비용은 0 이상이어야 합니다");
			return;
		}
		setError("");
		setSubmitting(true);
		const request = {
			name: name.trim(),
			checkIn,
			checkOut,
			cost: toCost(cost),
			reservationNo: reservationNo.trim() || null,
		};
		try {
			onSaved(lodging ? await updateLodging(lodging.id, request) : await createLodging(trip.id, request));
		} catch (err) {
			setError(getErrorMessage(err, "저장하지 못했어요. 잠시 후 다시 시도해주세요."));
			setSubmitting(false);
		}
	};

	const change = (setter) => (event) => {
		setter(event.target.value);
		setError("");
	};

	return (
		<form className={styles.form} onSubmit={handleSubmit} noValidate>
			<h3 className={styles.title}>{lodging ? "숙소 수정" : "숙소 추가"}</h3>
			<Input
				variant="box"
				label="이름"
				placeholder="예: 함덕 게스트하우스"
				autoFocus
				value={name}
				onChange={change(setName)}
			/>
			<Input variant="box" type="datetime-local" label="체크인" value={checkIn} onChange={change(setCheckIn)} />
			<Input
				variant="box"
				type="datetime-local"
				label="체크아웃"
				min={checkIn || undefined}
				value={checkOut}
				onChange={change(setCheckOut)}
			/>
			<Input
				variant="box"
				type="number"
				min="0"
				label="비용 (선택)"
				placeholder="원"
				value={cost}
				onChange={change(setCost)}
			/>
			<Input
				variant="box"
				label="예약번호 (선택)"
				helper="나만 볼 수 있어요 · 공유나 복사할 때는 빠져요"
				value={reservationNo}
				onChange={(event) => setReservationNo(event.target.value)}
			/>
			{error && <p className={styles.error}>{error}</p>}
			<div className={styles.actions}>
				<Button variant="secondary" onClick={onCancel} disabled={submitting}>
					취소
				</Button>
				<Button type="submit" disabled={submitting}>
					{submitting ? "저장 중…" : lodging ? "수정하기" : "추가하기"}
				</Button>
			</div>
		</form>
	);
}
