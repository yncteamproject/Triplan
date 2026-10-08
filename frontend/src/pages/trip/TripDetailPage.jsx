import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getErrorMessage } from "../../api/client";
import {
	deleteLodging,
	deleteStop,
	deleteTransportSegment,
	deleteTrip,
	getEstimate,
	getLodgings,
	getStops,
	getTransportSegments,
	getTrip,
} from "../../api/tripApi";
import Button from "../../components/common/Button";
import EmptyState from "../../components/common/EmptyState";
import Modal from "../../components/common/Modal";
import { useToast } from "../../context/ToastContext";
import {
	addDays,
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
import LodgingForm from "./LodgingForm";
import SegmentForm from "./SegmentForm";
import StopForm from "./StopForm";
import styles from "./TripDetailPage.module.css";
import TripEditModal from "./TripEditModal";
import { MODE_LABELS } from "./tripLabels";

const OUTSIDE = "outside"; // 여행 기간 밖 일정을 모아 보는 탭
const NOT_FOUND_STATUSES = [403, 404]; // 남의 여행 · 없는 여행

// 삭제 확인 창의 문구와 삭제 함수 (kind: trip | stop | segment | lodging)
const DELETE_CONFIRMS = {
	trip: {
		title: "여행을 삭제할까요?",
		description: "방문지 · 이동 구간 · 숙소와, 이 여행을 공유한 게시글도 함께 삭제돼요. 삭제하면 되돌릴 수 없어요.",
		done: "여행을 삭제했어요",
		remove: deleteTrip,
	},
	stop: {
		title: "방문지를 삭제할까요?",
		description: "이 방문지를 쓰는 이동 구간도 함께 삭제돼요.",
		done: "방문지를 삭제했어요",
		remove: deleteStop,
	},
	segment: {
		title: "이동 구간을 삭제할까요?",
		description: "삭제하면 되돌릴 수 없어요.",
		done: "이동 구간을 삭제했어요",
		remove: deleteTransportSegment,
	},
	lodging: {
		title: "숙소를 삭제할까요?",
		description: "삭제하면 되돌릴 수 없어요.",
		done: "숙소를 삭제했어요",
		remove: deleteLodging,
	},
};

const SAVED_MESSAGES = { stop: "방문지를 저장했어요", segment: "이동 구간을 저장했어요", lodging: "숙소를 저장했어요" };

// 값이 없는 것(null)은 뒤로 보낸다
const compareNullable = (a, b) => {
	if (a == null || b == null) {
		return (a == null) - (b == null);
	}
	return a < b ? -1 : a > b ? 1 : 0;
};

// 방문지 정렬: 날짜 → 방문 순서 → 시간 → 만든 순서
const compareStops = (a, b) =>
	compareNullable(a.date, b.date) ||
	compareNullable(a.stopOrder, b.stopOrder) ||
	compareNullable(a.time, b.time) ||
	a.id - b.id;

// 여행 상세: 왼쪽은 날짜별 일정, 오른쪽은 방문지 · 이동 구간 · 숙소 추가 · 수정 폼 (피그마 "📄 플래너 - 여행 상세 편집")
// 지도 · 장소 검색 · 대중교통 경로는 FE-7에서 오른쪽 패널에 붙인다
export default function TripDetailPage() {
	const { tripId } = useParams();
	const navigate = useNavigate();
	const { showToast } = useToast();
	const [reloadCount, setReloadCount] = useState(0); // "다시 시도"를 누르면 올려서 다시 불러온다
	const [result, setResult] = useState(null); // { key, status: ready | notFound | error, … }
	const [selectedDay, setSelectedDay] = useState(null);
	const [editing, setEditing] = useState(false);
	const [panel, setPanel] = useState(null); // 오른쪽에 열린 폼 { type: stop | segment | lodging, item: 수정할 대상(추가면 없음) }
	const [confirm, setConfirm] = useState(null); // 삭제 확인 창 { kind, item }
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

	const { trip, segments, lodgings, estimate } = result;
	const stops = [...result.stops].sort(compareStops);
	const days = listDates(trip.startDate, trip.endDate);
	const isOutside = (isoDate) => !days.includes(isoDate);
	const dayOf = (isoDate) => (isOutside(isoDate) ? OUTSIDE : isoDate);
	const hasOutside = stops.some((stop) => isOutside(stop.date)) || lodgings.some((lodging) => isOutside(dateOf(lodging.checkIn)));

	// 기간을 고쳐서 보던 날짜가 없어지면 첫째 날로 돌아간다
	const selectable = selectedDay === OUTSIDE ? hasOutside : days.includes(selectedDay);
	const activeDay = selectable ? selectedDay : days[0];
	const inActiveDay = (isoDate) => dayOf(isoDate) === activeDay;

	const dayStops = stops.filter((stop) => inActiveDay(stop.date));
	const dayLodgings = lodgings.filter((lodging) => inActiveDay(dateOf(lodging.checkIn)));
	const stopNames = new Map(stops.map((stop) => [stop.id, stop.name]));
	const segmentLabel = (segment) => `${stopNames.get(segment.fromStopId)} → ${stopNames.get(segment.toStopId)}`;

	const handleSaved = (updated) => {
		setResult({ ...result, trip: updated });
		setEditing(false);
		showToast("여행 정보를 수정했어요");
	};

	// 일정과 견적(총 경비)을 다시 불러온다. 화면 전체를 "불러오는 중"으로 바꾸지 않는다
	const refresh = async () => {
		try {
			const [nextStops, nextSegments, nextLodgings, nextEstimate] = await Promise.all([
				getStops(trip.id),
				getTransportSegments(trip.id),
				getLodgings(trip.id),
				getEstimate(trip.id),
			]);
			// 그 사이 다른 여행으로 넘어갔으면 버린다
			setResult((prev) =>
				prev?.key === loadKey && prev.status === "ready"
					? { ...prev, stops: nextStops, segments: nextSegments, lodgings: nextLodgings, estimate: nextEstimate }
					: prev,
			);
		} catch (err) {
			showToast(getErrorMessage(err, "일정을 다시 불러오지 못했어요. 새로고침 해주세요."));
		}
	};

	// 새 방문지 · 이동 구간 · 숙소 폼에 미리 채워둘 값
	const baseDate = activeDay === OUTSIDE ? trip.startDate : activeDay;
	const defaultsFor = (type) => {
		if (type === "stop") {
			const orders = stops.filter((stop) => stop.date === baseDate).map((stop) => stop.stopOrder ?? 0);
			return { date: baseDate, stopOrder: orders.length === 0 ? 1 : Math.max(...orders) + 1 };
		}
		if (type === "segment") {
			// 보고 있는 날의 첫 두 방문지를 출발지 · 도착지로 제안한다
			const [from, to] = dayStops.length >= 2 ? dayStops : stops;
			const departTime = `${from.date}T${from.time ? formatTime(from.time) : "09:00"}`;
			const toTime = to.time ? `${to.date}T${formatTime(to.time)}` : departTime;
			return {
				fromStopId: from.id,
				toStopId: to.id,
				departTime,
				arriveTime: toTime < departTime ? departTime : toTime,
			};
		}
		return { checkIn: `${baseDate}T15:00`, checkOut: `${addDays(baseDate, 1)}T11:00` };
	};

	const openPanel = (type, item) => setPanel({ type, item });
	const closePanel = () => setPanel(null);

	const handleItemSaved = (type, saved) => {
		closePanel();
		showToast(SAVED_MESSAGES[type]);
		// 저장한 일정이 있는 날짜 탭으로 옮겨서 바로 보이게 한다
		if (type === "stop") {
			setSelectedDay(dayOf(saved.date));
		} else if (type === "lodging") {
			setSelectedDay(dayOf(dateOf(saved.checkIn)));
		}
		refresh();
	};

	const closeConfirm = () => {
		if (!deleting) {
			setConfirm(null);
			setDeleteError("");
		}
	};

	const handleDelete = async () => {
		const { kind, item } = confirm;
		setDeleting(true);
		setDeleteError("");
		try {
			await DELETE_CONFIRMS[kind].remove(item.id);
		} catch (err) {
			setDeleteError(getErrorMessage(err, "삭제하지 못했어요. 잠시 후 다시 시도해주세요."));
			setDeleting(false);
			return;
		}
		showToast(DELETE_CONFIRMS[kind].done);
		if (kind === "trip") {
			navigate("/trips", { replace: true });
			return;
		}
		setDeleting(false);
		setConfirm(null);
		// 지운 것을 고치던 폼은 닫는다. 방문지를 지우면 이동 구간 폼의 선택지도 달라지므로 같이 닫는다
		if ((panel?.type === kind && panel.item?.id === item.id) || (kind === "stop" && panel?.type === "segment")) {
			closePanel();
		}
		refresh();
	};

	const confirmName = () => {
		if (confirm.kind === "trip") {
			return trip.title;
		}
		return confirm.kind === "segment" ? segmentLabel(confirm.item) : confirm.item.name;
	};

	const itemActions = (kind, item, label) => (
		<span className={styles.itemActions}>
			<button type="button" className={styles.itemButton} aria-label={`${label} 수정`} onClick={() => openPanel(kind, item)}>
				수정
			</button>
			<button
				type="button"
				className={`${styles.itemButton} ${styles.itemDelete}`}
				aria-label={`${label} 삭제`}
				onClick={() => setConfirm({ kind, item })}
			>
				삭제
			</button>
		</span>
	);

	const addButton = (type, label) => (
		<button
			type="button"
			className={panel?.type === type && !panel.item ? `${styles.addButton} ${styles.addButtonOn}` : styles.addButton}
			onClick={() => openPanel(type)}
		>
			{label}
		</button>
	);

	const renderPanel = () => {
		if (!panel) {
			return <p className={styles.sideHint}>추가할 것을 고르거나, 일정에서 "수정"을 눌러 고칠 수 있어요.</p>;
		}
		const { type, item } = panel;
		const key = `${type}-${item?.id ?? "new"}`; // 다른 대상을 열면 폼을 새로 만든다
		if (type === "stop") {
			return (
				<StopForm
					key={key}
					trip={trip}
					stop={item}
					defaults={defaultsFor("stop")}
					onCancel={closePanel}
					onSaved={(saved) => handleItemSaved("stop", saved)}
				/>
			);
		}
		if (type === "segment") {
			if (!item && stops.length < 2) {
				return (
					<EmptyState
						title="방문지를 2곳 이상 먼저 추가해주세요"
						description="이동 구간은 방문지와 방문지 사이를 잇는 일정이에요"
						action={<Button onClick={() => openPanel("stop")}>+ 방문지 추가</Button>}
					/>
				);
			}
			return (
				<SegmentForm
					key={key}
					trip={trip}
					stops={stops}
					segment={item}
					defaults={item ? null : defaultsFor("segment")}
					onCancel={closePanel}
					onSaved={(saved) => handleItemSaved("segment", saved)}
				/>
			);
		}
		return (
			<LodgingForm
				key={key}
				trip={trip}
				lodging={item}
				defaults={defaultsFor("lodging")}
				onCancel={closePanel}
				onSaved={(saved) => handleItemSaved("lodging", saved)}
			/>
		);
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
					<Button variant="text" className={styles.deleteButton} onClick={() => setConfirm({ kind: "trip", item: trip })}>
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

			<div className={styles.columns}>
				<div className={styles.main}>
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
							description="오른쪽에서 방문지와 숙소를 추가해보세요"
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
												{itemActions("stop", stop, stop.name)}
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
														{segment.reservationNo && <span>예약 {segment.reservationNo}</span>}
														{itemActions("segment", segment, segmentLabel(segment))}
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
											{lodging.reservationNo && <p className={styles.stopAddress}>예약 {lodging.reservationNo}</p>}
										</div>
										{lodging.cost != null && <span className={styles.lodgingCost}>{formatWon(lodging.cost)}</span>}
										{itemActions("lodging", lodging, lodging.name)}
									</div>
								);
							})}
						</div>
					)}
				</div>

				<aside className={styles.side}>
					<div className={styles.addButtons}>
						{addButton("stop", "+ 방문지")}
						{addButton("segment", "+ 이동 구간")}
						{addButton("lodging", "+ 숙소")}
					</div>
					{renderPanel()}
				</aside>
			</div>

			{editing && <TripEditModal trip={trip} onClose={() => setEditing(false)} onSaved={handleSaved} />}

			{confirm && (
				<Modal
					title={DELETE_CONFIRMS[confirm.kind].title}
					onClose={closeConfirm}
					footer={
						<>
							<Button variant="secondary" onClick={closeConfirm} disabled={deleting}>
								취소
							</Button>
							<Button variant="danger" onClick={handleDelete} disabled={deleting}>
								{deleting ? "삭제 중…" : "삭제"}
							</Button>
						</>
					}
				>
					<p>
						<strong className={styles.confirmName}>{confirmName()}</strong>
					</p>
					<p>{DELETE_CONFIRMS[confirm.kind].description}</p>
					{deleteError && <p className={styles.error}>{deleteError}</p>}
				</Modal>
			)}
		</section>
	);
}
