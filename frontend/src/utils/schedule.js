import { dateOf, listDates } from "./date";

// 여행 일정을 날짜(Day 탭)별로 나누는 계산. 여행 상세와 게시글 상세(공유된 여행)가 같이 쓴다

export const OUTSIDE = "outside"; // 여행 기간 밖 일정을 모아 보는 탭

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

// 그날 쓰는 숙소인지: 체크인 날부터 체크아웃하는 날까지
// (체크아웃하는 날 아침에도 그 숙소에서 출발하므로 같이 보여준다)
const staysOn = (lodging, isoDate) => dateOf(lodging.checkIn) <= isoDate && isoDate <= dateOf(lodging.checkOut);

// trip: { startDate, endDate }, selectedDay: 사용자가 고른 탭(날짜 또는 OUTSIDE, 없으면 첫째 날)
export function buildSchedule({ trip, stops, lodgings, selectedDay }) {
	const sortedStops = [...stops].sort(compareStops);
	const days = listDates(trip.startDate, trip.endDate);
	const isOutside = (isoDate) => !days.includes(isoDate);

	// 방문지 날짜가 속한 탭
	const dayOf = (isoDate) => (isOutside(isoDate) ? OUTSIDE : isoDate);
	// 숙소가 처음 보이는 탭. 여행 기간과 전혀 겹치지 않으면 "기간 밖"
	const lodgingDay = (lodging) => days.find((day) => staysOn(lodging, day)) ?? OUTSIDE;

	const hasOutside =
		sortedStops.some((stop) => isOutside(stop.date)) || lodgings.some((lodging) => lodgingDay(lodging) === OUTSIDE);

	// 기간을 고쳐서 보던 날짜가 없어지면 첫째 날로 돌아간다
	const selectable = selectedDay === OUTSIDE ? hasOutside : days.includes(selectedDay);
	const activeDay = selectable ? selectedDay : days[0];

	return {
		days,
		stops: sortedStops,
		hasOutside,
		activeDay,
		dayOf,
		lodgingDay,
		dayStops: sortedStops.filter((stop) => dayOf(stop.date) === activeDay),
		dayLodgings: lodgings.filter((lodging) =>
			activeDay === OUTSIDE ? lodgingDay(lodging) === OUTSIDE : staysOn(lodging, activeDay),
		),
	};
}
