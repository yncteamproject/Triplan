import { useState } from "react";
import { getErrorMessage } from "../../api/client";
import { createStop, updateStop } from "../../api/tripApi";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import Textarea from "../../components/common/Textarea";
import PlaceSearch from "../../components/trip/PlaceSearch";
import { formatPeriod, formatTime } from "../../utils/date";
import styles from "./TripForms.module.css";

// 방문지 추가 · 수정 폼. stop이 있으면 수정, 없으면 추가
// defaults: 새 방문지에 미리 채워둘 값 { date, stopOrder, name?, time? } (name · time은 숙소를 방문지로 추가할 때만)
// 위치(위도 · 경도)는 장소 검색으로 고른 곳에서만 들어온다. 직접 입력하면 주소만 저장된다
export default function StopForm({ trip, stop, defaults, onCancel, onSaved }) {
	const [name, setName] = useState(stop?.name ?? defaults.name ?? "");
	const [date, setDate] = useState(stop?.date ?? defaults.date);
	const [time, setTime] = useState(stop?.time ? formatTime(stop.time) : (defaults.time ?? ""));
	const [stopOrder, setStopOrder] = useState(String(stop ? (stop.stopOrder ?? "") : defaults.stopOrder));
	const [address, setAddress] = useState(stop?.address ?? "");
	// 지도에 찍을 위치 { latitude, longitude }. 없으면 null
	const [location, setLocation] = useState(
		stop?.latitude != null && stop?.longitude != null ? { latitude: stop.latitude, longitude: stop.longitude } : null,
	);
	const [memo, setMemo] = useState(stop?.memo ?? "");
	const [error, setError] = useState("");
	const [submitting, setSubmitting] = useState(false);

	const outsidePeriod = date !== "" && (date < trip.startDate || date > trip.endDate);

	const handleSubmit = async (event) => {
		event.preventDefault();
		if (!name.trim()) {
			setError("방문지 이름을 입력해주세요");
			return;
		}
		if (!date) {
			setError("방문 날짜를 선택해주세요");
			return;
		}
		if (stopOrder !== "" && !/^\d+$/.test(stopOrder)) {
			setError("방문 순서는 0 이상이어야 합니다");
			return;
		}
		setError("");
		setSubmitting(true);
		// 수정(PUT)은 보낸 값으로 전부 바뀐다. 폼에 없는 값(사진 주소)은 기존 값을 그대로 보낸다
		const request = {
			name: name.trim(),
			date,
			time: time || null,
			memo: memo.trim() || null,
			imageUrl: stop?.imageUrl ?? null,
			stopOrder: stopOrder === "" ? null : Number(stopOrder),
			// 위도와 경도는 함께 보내야 한다 (서버 검증)
			latitude: location?.latitude ?? null,
			longitude: location?.longitude ?? null,
			address: address.trim() || null,
		};
		try {
			onSaved(stop ? await updateStop(stop.id, request) : await createStop(trip.id, request));
		} catch (err) {
			setError(getErrorMessage(err, "저장하지 못했어요. 잠시 후 다시 시도해주세요."));
			setSubmitting(false);
		}
	};

	const clearError = () => setError("");

	// 검색 결과에서 고른 장소로 이름 · 주소 · 위치를 채운다
	const applyPlace = (place) => {
		setName(place.name);
		setAddress(place.address ?? "");
		setLocation({ latitude: place.latitude, longitude: place.longitude });
		clearError();
	};

	return (
		<form className={styles.form} onSubmit={handleSubmit} noValidate>
			<h3 className={styles.title}>{stop ? "방문지 수정" : "방문지 추가"}</h3>
			<PlaceSearch onSelect={applyPlace} />
			<Input
				variant="box"
				label="이름"
				placeholder="예: 성산일출봉"
				autoFocus
				value={name}
				onChange={(event) => {
					setName(event.target.value);
					clearError();
				}}
			/>
			<div className={styles.row}>
				<Input
					variant="box"
					type="date"
					label="날짜"
					value={date}
					onChange={(event) => {
						setDate(event.target.value);
						clearError();
					}}
				/>
				<Input
					variant="box"
					type="time"
					label="시간 (선택)"
					value={time}
					onChange={(event) => setTime(event.target.value)}
				/>
			</div>
			{outsidePeriod && (
				<p className={styles.notice}>
					여행 기간({formatPeriod(trip.startDate, trip.endDate)}) 밖의 날짜예요. "기간 밖" 탭에 보여요.
				</p>
			)}
			<Input
				variant="box"
				type="number"
				min="0"
				label="방문 순서"
				helper="같은 날에는 숫자가 작은 방문지가 먼저 보여요"
				value={stopOrder}
				onChange={(event) => {
					setStopOrder(event.target.value);
					clearError();
				}}
			/>
			<Input
				variant="box"
				label="주소 (선택)"
				placeholder="예: 제주특별자치도 서귀포시 성산읍"
				maxLength={255}
				value={address}
				onChange={(event) => setAddress(event.target.value)}
			/>
			{location ? (
				<p className={styles.located}>
					<span>지도에 표시되는 위치가 있어요</span>
					<button type="button" className={styles.locatedClear} onClick={() => setLocation(null)}>
						위치 지우기
					</button>
				</p>
			) : (
				<p className={styles.hint}>위치가 없으면 지도에는 나오지 않아요. 장소 검색으로 고르면 위치가 들어가요.</p>
			)}
			<Textarea label="메모 (선택)" value={memo} onChange={(event) => setMemo(event.target.value)} />
			{error && <p className={styles.error}>{error}</p>}
			<div className={styles.actions}>
				<Button variant="secondary" onClick={onCancel} disabled={submitting}>
					취소
				</Button>
				<Button type="submit" disabled={submitting}>
					{submitting ? "저장 중…" : stop ? "수정하기" : "추가하기"}
				</Button>
			</div>
		</form>
	);
}
