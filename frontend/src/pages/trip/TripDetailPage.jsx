import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getErrorMessage } from "../../api/client";
import { deleteTrip, getEstimate, getLodgings, getStops, getTransportSegments, getTrip } from "../../api/tripApi";
import Button from "../../components/common/Button";
import EmptyState from "../../components/common/EmptyState";
import Modal from "../../components/common/Modal";
import { useToast } from "../../context/ToastContext";
import {
	countMinutes,
	countNights,
	dateOf,
	formatDayLabel,
	formatDuration,
	formatNights,
	formatPeriod,
	formatTime,
	listDates,
	timeOf,
} from "../../utils/date";
import { formatWon } from "../../utils/format";
import styles from "./TripDetailPage.module.css";
import TripEditModal from "./TripEditModal";

const MODE_LABELS = { FLIGHT: "비행기", TRAIN: "기차", BUS: "버스", CAR: "자동차", WALK: "도보" };
const OUTSIDE = "outside"; // 여행 기간 밖 일정을 모아 보는 탭
const NOT_FOUND_STATUSES = [403, 404]; // 남의 여행 · 없는 여행

// 여행 상세: 날짜별 일정 보기 + 여행 수정 · 삭제 (피그마 "📄 플래너 - 여행 상세 편집"의 왼쪽 일정 부분)
// 방문지 · 이동 구간 · 숙소 추가 · 수정은 FE-6에서 만든다
export default function TripDetailPage() {
	const { tripId } = useParams();
	const navigate = useNavigate();
	const { showToast } = useToast();
	const [reloadCount, setReloadCount] = useState(0); // "다시 시도"를 누르면 올려서 다시 불러온다
	const [result, setResult] = useState(null); // { key, status: ready | notFound | error, … }
	const [selectedDay, setSelectedDay] = useState(null);
	const [editing, setEditing] = useState(false);
	const [confirmingDelete, setConfirmingDelete] = useState(false);
	const [deleting, setDeleting] = useState(false);
	const [deleteError, setDeleteError] = useState("");

	// 주소의 id가 숫자가 아니면 서버에 묻지 않고 바로 "찾을 수 없어요"를 보여준다
	// (서버가 이 경우 401을 줘서, 그대로 보내면 로그아웃돼 버린다)
	const validId = /^\d+$/.test(tripId);

	// 어떤 요청의 결과인지 구분하는 값. 결과의 key가 다르면 아직 불러오는 중이다
	const loadKey = `${tripId}:${reloadCount}`;

	useEffect(() => {
		if (!validId) {
			return undefined;
		}
		let cancelled = false;
		Promise.all([
			getTrip(tripId),
			getStops(tripId),
			getTransportSegments(tripId),
			getLodgings(tripId),
			getEstimate(tripId),
		])
			.then(([trip, stops, segments, lodgings, estimate]) => {
				if (!cancelled) {
					setResult({ key: loadKey, status: "ready", trip, stops, segments, lodgings, estimate });
				}
			})
			.catch((err) => {
				if (cancelled) {
					return;
				}
				if (NOT_FOUND_STATUSES.includes(err.response?.status)) {
					setResult({ key: loadKey, status: "notFound" });
				} else {
					setResult({
						key: loadKey,
						status: "error",
						message: getErrorMessage(err, "여행을 불러오지 못했어요."),
					});
				}
			});
		// 화면을 떠난 뒤에 응답이 오면 무시한다
		return () => {
			cancelled = true;
		};
	}, [tripId, loadKey, validId]);

	if (validId && result?.key !== loadKey) {
		return (
			<section className={styles.page}>
				<p className={styles.loading}>여행을 불러오는 중이에요…</p>
			</section>
		);
	}

	if (!validId || result.status === "notFound") {
		return (
			<section className={styles.page}>
				<EmptyState
					title="여행을 찾을 수 없어요"
					description="삭제됐거나 볼 수 없는 여행이에요"
					action={<Button to="/trips">내 여행 목록으로</Button>}
				/>
			</section>
		);
	}

	if (result.status === "error") {
		return (
			<section className={styles.page}>
				<EmptyState
					title={result.message}
					description="잠시 후 다시 시도해주세요"
					action={
						<Button variant="secondary" onClick={() => setReloadCount((count) => count + 1)}>
							다시 시도
						</Button>
					}
				/>
			</section>
		);
	}

	const { trip, stops, segments, lodgings, estimate } = result;
	const days = listDates(trip.startDate, trip.endDate);
	const isOutside = (isoDate) => !days.includes(isoDate);
	const hasOutside = stops.some((stop) => isOutside(stop.date)) || lodgings.some((lodging) => isOutside(dateOf(lodging.checkIn)));

	// 기간을 고쳐서 보던 날짜가 없어지면 첫째 날로 돌아간다
	const selectable = selectedDay === OUTSIDE ? hasOutside : days.includes(selectedDay);
	const activeDay = selectable ? selectedDay : days[0];
	const inActiveDay = (isoDate) => (activeDay === OUTSIDE ? isOutside(isoDate) : isoDate === activeDay);

	const dayStops = stops.filter((stop) => inActiveDay(stop.date));
	const dayLodgings = lodgings.filter((lodging) => inActiveDay(dateOf(lodging.checkIn)));
	const stopNames = new Map(stops.map((stop) => [stop.id, stop.name]));

	const handleSaved = (updated) => {
		setResult({ ...result, trip: updated });
		setEditing(false);
		showToast("여행 정보를 수정했어요");
	};

	const closeDeleteConfirm = () => {
		if (!deleting) {
			setConfirmingDelete(false);
			setDeleteError("");
		}
	};

	const handleDelete = async () => {
		setDeleting(true);
		setDeleteError("");
		try {
			await deleteTrip(trip.id);
			showToast("여행을 삭제했어요");
			navigate("/trips", { replace: true });
		} catch (err) {
			setDeleteError(getErrorMessage(err, "삭제하지 못했어요. 잠시 후 다시 시도해주세요."));
			setDeleting(false);
		}
	};

	return (
		<section className={styles.page}>
			<header className={styles.head}>
				<div className={styles.headInfo}>
					<h2 className={styles.title}>{trip.title}</h2>
					<p className={styles.period}>
						{formatPeriod(trip.startDate, trip.endDate)} · {formatNights(trip.startDate, trip.endDate)}
					</p>
				</div>
				<div className={styles.headActions}>
					<Button variant="secondary" onClick={() => setEditing(true)}>
						여행 정보 수정
					</Button>
					<Button variant="text" className={styles.deleteButton} onClick={() => setConfirmingDelete(true)}>
						삭제
					</Button>
				</div>
			</header>

			<ul className={styles.summary}>
				<li className={styles.summaryCard}>
					<span className={styles.summaryLabel}>총 일정</span>
					<span className={styles.summaryValue}>{stops.length}곳</span>
				</li>
				<li className={styles.summaryCard}>
					<span className={styles.summaryLabel}>총 경비</span>
					<span className={`${styles.summaryValue} ${styles.summaryPrimary}`}>{formatWon(estimate.totalCost)}</span>
				</li>
				<li className={styles.summaryCard}>
					<span className={styles.summaryLabel}>총 교통비</span>
					<span className={styles.summaryValue}>{formatWon(estimate.transportCost)}</span>
				</li>
				<li className={styles.summaryCard}>
					<span className={styles.summaryLabel}>총 숙박비</span>
					<span className={styles.summaryValue}>{formatWon(estimate.lodgingCost)}</span>
				</li>
			</ul>

			<div className={styles.tabs} role="tablist">
				{days.map((day, index) => (
					<button
						key={day}
						type="button"
						role="tab"
						aria-selected={day === activeDay}
						className={day === activeDay ? `${styles.tab} ${styles.tabOn}` : styles.tab}
						onClick={() => setSelectedDay(day)}
					>
						<span className={styles.tabDay}>Day {index + 1}</span>
						<span className={styles.tabDate}>{formatDayLabel(day)}</span>
					</button>
				))}
				{hasOutside && (
					<button
						type="button"
						role="tab"
						aria-selected={activeDay === OUTSIDE}
						className={activeDay === OUTSIDE ? `${styles.tab} ${styles.tabOn}` : styles.tab}
						onClick={() => setSelectedDay(OUTSIDE)}
					>
						<span className={styles.tabDay}>기간 밖</span>
						<span className={styles.tabDate}>여행 기간이 아닌 일정</span>
					</button>
				)}
			</div>

			{dayStops.length === 0 && dayLodgings.length === 0 ? (
				<EmptyState
					title={stops.length === 0 && lodgings.length === 0 ? "아직 일정이 없어요" : "이 날은 일정이 없어요"}
					description="방문지와 숙소를 추가하면 여기에 보여요"
				/>
			) : (
				<div className={styles.schedule}>
					{dayStops.length > 0 && (
						<ol className={styles.timeline}>
							{dayStops.map((stop, index) => (
								<li key={stop.id} className={styles.timelineItem}>
									<div className={styles.stop}>
										<span className={styles.stopTime}>
											{activeDay === OUTSIDE && <span className={styles.stopDate}>{formatDayLabel(stop.date)}</span>}
											{stop.time ? formatTime(stop.time) : "시간 미정"}
										</span>
										<div className={styles.stopBody}>
											<p className={styles.stopName}>{stop.name}</p>
											{stop.address && <p className={styles.stopAddress}>{stop.address}</p>}
											{stop.memo && <p className={styles.stopMemo}>{stop.memo}</p>}
										</div>
									</div>
									{segments
										.filter((segment) => segment.fromStopId === stop.id)
										.map((segment) => (
											<p key={segment.id} className={styles.segment}>
												<span className={styles.segmentMode}>{MODE_LABELS[segment.mode] ?? segment.mode}</span>
												{/* 바로 다음 방문지로 가는 구간이 아니면 도착지를 적어준다 */}
												{dayStops[index + 1]?.id !== segment.toStopId && (
													<span>→ {stopNames.get(segment.toStopId)}</span>
												)}
												<span>
													{timeOf(segment.departTime)} - {timeOf(segment.arriveTime)}
												</span>
												<span>{formatDuration(countMinutes(segment.departTime, segment.arriveTime))}</span>
												{segment.cost != null && <span>{formatWon(segment.cost)}</span>}
											</p>
										))}
								</li>
							))}
						</ol>
					)}

					{dayLodgings.map((lodging) => {
						const nights = countNights(dateOf(lodging.checkIn), dateOf(lodging.checkOut));
						return (
							<div key={lodging.id} className={styles.lodging}>
								<span className={styles.lodgingBadge}>숙소</span>
								<div className={styles.stopBody}>
									<p className={styles.stopName}>{lodging.name}</p>
									<p className={styles.stopAddress}>
										{formatDayLabel(dateOf(lodging.checkIn))} {timeOf(lodging.checkIn)} 체크인 ·{" "}
										{formatDayLabel(dateOf(lodging.checkOut))} {timeOf(lodging.checkOut)} 체크아웃 ·{" "}
										{nights <= 0 ? "당일" : `${nights}박`}
									</p>
								</div>
								{lodging.cost != null && <span className={styles.lodgingCost}>{formatWon(lodging.cost)}</span>}
							</div>
						);
					})}
				</div>
			)}

			{editing && <TripEditModal trip={trip} onClose={() => setEditing(false)} onSaved={handleSaved} />}

			{confirmingDelete && (
				<Modal
					title="여행을 삭제할까요?"
					onClose={closeDeleteConfirm}
					footer={
						<>
							<Button variant="secondary" onClick={closeDeleteConfirm} disabled={deleting}>
								취소
							</Button>
							<Button variant="danger" onClick={handleDelete} disabled={deleting}>
								{deleting ? "삭제 중…" : "삭제"}
							</Button>
						</>
					}
				>
					<p>
						<strong className={styles.confirmName}>{trip.title}</strong> 여행을 삭제해요.
					</p>
					<p>방문지 · 이동 구간 · 숙소와, 이 여행을 공유한 게시글도 함께 삭제돼요. 삭제하면 되돌릴 수 없어요.</p>
					{deleteError && <p className={styles.error}>{deleteError}</p>}
				</Modal>
			)}
		</section>
	);
}
