// 서버가 주는 날짜("2026-09-10")를 화면용으로 바꾸는 함수들
// new Date("2026-09-10")는 시간대에 따라 하루가 밀릴 수 있어서 숫자로 직접 나눈다

const toParts = (isoDate) => isoDate.split("-").map(Number);

const toUtc = (isoDate) => {
	const [year, month, day] = toParts(isoDate);
	return Date.UTC(year, month - 1, day);
};

const pad = (value) => String(value).padStart(2, "0");

// "2026-09-10" → "2026.09.10"
export const formatDate = (isoDate) => isoDate.replaceAll("-", ".");

// "2026.09.10 - 09.13" (연도가 다르면 "2026.12.30 - 2027.01.02")
export const formatPeriod = (startDate, endDate) => {
	const [startYear] = toParts(startDate);
	const [endYear, endMonth, endDay] = toParts(endDate);
	const end = startYear === endYear ? `${pad(endMonth)}.${pad(endDay)}` : formatDate(endDate);
	return `${formatDate(startDate)} - ${end}`;
};

// 시작일과 종료일 사이의 밤 수 (같은 날이면 0)
export const countNights = (startDate, endDate) =>
	Math.round((toUtc(endDate) - toUtc(startDate)) / (24 * 60 * 60 * 1000));

// "3박 4일", 같은 날이면 "당일치기"
export const formatNights = (startDate, endDate) => {
	const nights = countNights(startDate, endDate);
	return nights <= 0 ? "당일치기" : `${nights}박 ${nights + 1}일`;
};

// "9월"
export const formatMonth = (isoDate) => `${toParts(isoDate)[1]}월`;

const DAY_MS = 24 * 60 * 60 * 1000;
const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"];

// 시작일부터 종료일까지의 날짜 목록 ["2026-09-10", "2026-09-11", …]
export const listDates = (startDate, endDate) => {
	const dates = [];
	for (let time = toUtc(startDate); time <= toUtc(endDate); time += DAY_MS) {
		dates.push(new Date(time).toISOString().slice(0, 10));
	}
	return dates;
};

// "2026-09-10" → "9.10 (목)"
export const formatDayLabel = (isoDate) => {
	const [, month, day] = toParts(isoDate);
	return `${month}.${day} (${WEEKDAYS[new Date(toUtc(isoDate)).getUTCDay()]})`;
};

// "09:30:00" → "09:30"
export const formatTime = (time) => time.slice(0, 5);

// 서버의 날짜+시간("2026-09-10T09:30:00")에서 날짜 · 시간만 꺼낸다
export const dateOf = (dateTime) => dateTime.slice(0, 10);
export const timeOf = (dateTime) => dateTime.slice(11, 16);

// 두 날짜+시간 사이의 분
export const countMinutes = (fromDateTime, toDateTime) => {
	const toMinutes = (dateTime) => {
		const [hour, minute] = timeOf(dateTime).split(":").map(Number);
		return toUtc(dateOf(dateTime)) / 60000 + hour * 60 + minute;
	};
	return toMinutes(toDateTime) - toMinutes(fromDateTime);
};

// 80 → "1시간 20분", 20 → "20분", 120 → "2시간"
export const formatDuration = (minutes) => {
	const hours = Math.floor(minutes / 60);
	const rest = minutes % 60;
	if (hours === 0) {
		return `${rest}분`;
	}
	return rest === 0 ? `${hours}시간` : `${hours}시간 ${rest}분`;
};

// "2026-09-10" + 1 → "2026-09-11"
export const addDays = (isoDate, days) => new Date(toUtc(isoDate) + days * DAY_MS).toISOString().slice(0, 10);

// 서버의 날짜+시간 → <input type="datetime-local"> 값 ("2026-09-10T09:30")
export const toDateTimeInput = (dateTime) => dateTime.slice(0, 16);
