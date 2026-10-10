import {
	countMinutes,
	countNights,
	dateOf,
	formatDayLabel,
	formatDuration,
	formatTime,
	timeOf,
} from "../../utils/date";
import { formatWon } from "../../utils/format";
import { OUTSIDE } from "../../utils/schedule";
import { MODE_LABELS } from "../../utils/tripLabels";
import EmptyState from "../common/EmptyState";
import styles from "./TripSchedule.module.css";

// 여행 일정 보기: 요약 카드 → 그날 숙소 → Day 탭 → 날짜별 방문지 · 이동 구간
// 여행 상세(편집 가능)와 게시글 상세(공유된 여행, 보기만)가 같이 쓴다
//
// schedule: buildSchedule()의 결과, costs: { totalCost, transportCost, lodgingCost }
// renderActions(kind, item): 카드에 붙일 버튼들(수정 · 삭제 등). 안 주면 보기 전용
// side: 일정 오른쪽에 둘 패널(추가 · 수정 폼). 안 주면 일정이 전체 너비를 쓴다
// 예약번호는 값이 있을 때만 보인다 (공유된 여행에는 서버가 주지 않는다, B12)
export default function TripSchedule({
	schedule,
	segments,
	lodgings,
	costs,
	onSelectDay,
	renderActions,
	side,
	emptyDescription,
}) {
	const { days, stops, hasOutside, activeDay, dayStops, dayLodgings } = schedule;
	const stopNames = new Map(stops.map((stop) => [stop.id, stop.name]));
	const actions = (kind, item) => renderActions && <span className={styles.actions}>{renderActions(kind, item)}</span>;

	const timeline = (
		<div className={styles.main}>
			{dayStops.length === 0 ? (
				<EmptyState
					title={stops.length === 0 ? "아직 일정이 없어요" : "이 날은 일정이 없어요"}
					description={emptyDescription}
				/>
			) : (
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
								{actions("stop", stop)}
							</div>
							{segments
								.filter((segment) => segment.fromStopId === stop.id)
								.map((segment) => (
									<p key={segment.id} className={styles.segment}>
										<span className={styles.segmentMode}>{MODE_LABELS[segment.mode] ?? segment.mode}</span>
										{/* 바로 다음 방문지로 가는 구간이 아니면 도착지를 적어준다 */}
										{dayStops[index + 1]?.id !== segment.toStopId && <span>→ {stopNames.get(segment.toStopId)}</span>}
										<span>
											{timeOf(segment.departTime)} - {timeOf(segment.arriveTime)}
										</span>
										<span>{formatDuration(countMinutes(segment.departTime, segment.arriveTime))}</span>
										{segment.cost != null && <span>{formatWon(segment.cost)}</span>}
										{segment.reservationNo && <span>예약 {segment.reservationNo}</span>}
										{actions("segment", segment)}
									</p>
								))}
						</li>
					))}
				</ol>
			)}
		</div>
	);

	return (
		<>
			<ul className={styles.summary}>
				<li className={styles.summaryCard}>
					<span className={styles.summaryLabel}>총 일정</span>
					<span className={styles.summaryValue}>{stops.length}곳</span>
				</li>
				<li className={styles.summaryCard}>
					<span className={styles.summaryLabel}>총 경비</span>
					<span className={`${styles.summaryValue} ${styles.summaryPrimary}`}>{formatWon(costs.totalCost)}</span>
				</li>
				<li className={styles.summaryCard}>
					<span className={styles.summaryLabel}>총 교통비</span>
					<span className={styles.summaryValue}>{formatWon(costs.transportCost)}</span>
					<span className={styles.summaryCount}>이동 {segments.length}구간</span>
				</li>
				<li className={styles.summaryCard}>
					<span className={styles.summaryLabel}>총 숙박비</span>
					<span className={styles.summaryValue}>{formatWon(costs.lodgingCost)}</span>
					<span className={styles.summaryCount}>숙소 {lodgings.length}곳</span>
				</li>
			</ul>

			{/* 숙소는 일정과 따로, Day 탭 위에 크게 보여준다. 보고 있는 날에 쓰는 숙소만 나온다 */}
			{lodgings.length > 0 && (
				<div className={styles.lodgings}>
					{dayLodgings.length === 0 && (
						// 탭을 바꿀 때 아래 내용이 오르내리지 않게, 숙소가 없는 날에도 자리를 지킨다
						<div className={`${styles.lodging} ${styles.lodgingNone}`}>
							<span className={styles.lodgingBadge}>이 날 숙소</span>
							<p className={styles.lodgingEmpty}>이 날은 묵는 숙소가 없어요</p>
						</div>
					)}
					{dayLodgings.map((lodging) => {
						const checkInDate = dateOf(lodging.checkIn);
						const checkOutDate = dateOf(lodging.checkOut);
						const nights = countNights(checkInDate, checkOutDate);
						return (
							<div key={lodging.id} className={styles.lodging}>
								<div className={styles.lodgingHead}>
									<span className={styles.lodgingBadge}>{activeDay === OUTSIDE ? "숙소" : "이 날 숙소"}</span>
									{activeDay !== OUTSIDE && nights > 0 && (
										<span className={styles.lodgingStatus}>
											{activeDay === checkOutDate
												? "체크아웃하는 날"
												: nights > 1
													? `${nights}박 중 ${countNights(checkInDate, activeDay) + 1}번째 밤`
													: "체크인하는 날"}
										</span>
									)}
									{actions("lodging", lodging)}
								</div>
								<p className={styles.lodgingName}>{lodging.name}</p>
								<dl className={styles.lodgingInfo}>
									<div>
										<dt>체크인</dt>
										<dd>
											{formatDayLabel(checkInDate)} {timeOf(lodging.checkIn)}
										</dd>
									</div>
									<div>
										<dt>체크아웃</dt>
										<dd>
											{formatDayLabel(checkOutDate)} {timeOf(lodging.checkOut)}
										</dd>
									</div>
									<div>
										<dt>숙박</dt>
										<dd>{nights <= 0 ? "당일" : `${nights}박`}</dd>
									</div>
									{lodging.cost != null && (
										<div>
											<dt>비용</dt>
											<dd>{formatWon(lodging.cost)}</dd>
										</div>
									)}
									{lodging.reservationNo && (
										<div>
											<dt>예약번호</dt>
											<dd>{lodging.reservationNo}</dd>
										</div>
									)}
								</dl>
							</div>
						);
					})}
				</div>
			)}

			<div className={styles.tabs} role="tablist">
				{days.map((day, index) => (
					<button
						key={day}
						type="button"
						role="tab"
						aria-selected={day === activeDay}
						className={day === activeDay ? `${styles.tab} ${styles.tabOn}` : styles.tab}
						onClick={() => onSelectDay(day)}
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
						onClick={() => onSelectDay(OUTSIDE)}
					>
						<span className={styles.tabDay}>기간 밖</span>
						<span className={styles.tabDate}>여행 기간이 아닌 일정</span>
					</button>
				)}
			</div>

			{side ? (
				<div className={styles.columns}>
					{timeline}
					{side}
				</div>
			) : (
				timeline
			)}
		</>
	);
}
