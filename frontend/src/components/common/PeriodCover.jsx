import { formatMonth, formatNights } from "../../utils/date";
import styles from "./PeriodCover.module.css";

const TONES = ["blue", "purple", "orange"];

// 사진 대신 쓰는 여행 썸네일: 연한 색 바탕 + "3박 4일 / 9월"
// 색은 id로 골라서 같은 여행은 항상 같은 색이다. 지도 썸네일(T6)이 없을 때의 대체 커버로도 쓴다
export default function PeriodCover({ id, startDate, endDate, size = "sm" }) {
	const tone = TONES[Math.abs(Number(id) || 0) % TONES.length];
	return (
		<div className={`${styles.cover} ${styles[tone]} ${styles[size]}`}>
			<span className={styles.nights}>{formatNights(startDate, endDate)}</span>
			<span className={styles.month}>{formatMonth(startDate)}</span>
		</div>
	);
}
