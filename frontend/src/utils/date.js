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
