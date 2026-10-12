import { useState } from "react";
import { getErrorMessage } from "../../api/client";
import { createTransportSegment, updateTransportSegment } from "../../api/tripApi";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import Select from "../../components/common/Select";
import { formatDayLabel, toDateTimeInput } from "../../utils/date";
import { isValidCost, toCost } from "../../utils/format";
import { MODE_OPTIONS } from "../../utils/tripLabels";
import styles from "./TripForms.module.css";

// 이동 구간 추가 · 수정 폼. segment가 있으면 수정, 없으면 추가 (defaults: 새 구간의 기본 출발지 · 도착지 · 시간)
// 출발지 · 도착지는 이 여행의 방문지 중에서만 고른다 (B10)
export default function SegmentForm({ trip, stops, segment, defaults, onCancel, onSaved }) {
	const [fromStopId, setFromStopId] = useState(String(segment?.fromStopId ?? defaults.fromStopId));
	const [toStopId, setToStopId] = useState(String(segment?.toStopId ?? defaults.toStopId));
	const [mode, setMode] = useState(segment?.mode ?? "");
	const [departTime, setDepartTime] = useState(segment ? toDateTimeInput(segment.departTime) : defaults.departTime);
	const [arriveTime, setArriveTime] = useState(segment ? toDateTimeInput(segment.arriveTime) : defaults.arriveTime);
	const [cost, setCost] = useState(segment?.cost != null ? String(segment.cost) : "");
	const [reservationNo, setReservationNo] = useState(segment?.reservationNo ?? "");
	const [error, setError] = useState("");
	const [submitting, setSubmitting] = useState(false);

	const stopOptions = stops.map((stop) => ({
		value: String(stop.id),
		label: `${formatDayLabel(stop.date)} ${stop.name}`,
	}));

	const handleSubmit = async (event) => {
		event.preventDefault();
		if (!fromStopId || !toStopId) {
			setError("출발지와 도착지를 선택해주세요");
			return;
		}
		if (fromStopId === toStopId) {
			setError("출발지와 도착지가 같아요");
			return;
		}
		if (!mode) {
			setError("이동 수단을 선택해주세요");
			return;
		}
		if (!departTime || !arriveTime) {
			setError("출발 시간과 도착 시간을 입력해주세요");
			return;
		}
		// 서버도 같은 검사를 한다 (B6, B7)
		if (arriveTime < departTime) {
			setError("도착 시간은 출발 시간보다 빠를 수 없습니다");
			return;
		}
		if (!isValidCost(cost)) {
			setError("비용은 0 이상이어야 합니다");
			return;
		}
		setError("");
		setSubmitting(true);
		const request = {
			fromStopId: Number(fromStopId),
			toStopId: Number(toStopId),
			mode,
			departTime,
			arriveTime,
			cost: toCost(cost),
			reservationNo: reservationNo.trim() || null,
		};
		try {
			onSaved(
				segment
					? await updateTransportSegment(segment.id, request)
					: await createTransportSegment(trip.id, request),
			);
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
			<h3 className={styles.title}>{segment ? "이동 구간 수정" : "이동 구간 추가"}</h3>
			<Select
				label="출발지"
				placeholder="방문지 선택"
				options={stopOptions}
				value={fromStopId}
				onChange={change(setFromStopId)}
			/>
			<Select
				label="도착지"
				placeholder="방문지 선택"
				options={stopOptions}
				value={toStopId}
				onChange={change(setToStopId)}
			/>
			<p className={styles.hint}>숙소를 고르려면 숙소 카드의 "방문지로 추가"를 먼저 눌러주세요.</p>
			<Select
				label="이동 수단"
				placeholder="수단 선택"
				options={MODE_OPTIONS}
				value={mode}
				onChange={change(setMode)}
			/>
			<Input
				variant="box"
				type="datetime-local"
				label="출발 시간"
				value={departTime}
				onChange={change(setDepartTime)}
			/>
			<Input
				variant="box"
				type="datetime-local"
				label="도착 시간"
				min={departTime || undefined}
				value={arriveTime}
				onChange={change(setArriveTime)}
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
					{submitting ? "저장 중…" : segment ? "수정하기" : "추가하기"}
				</Button>
			</div>
		</form>
	);
}
