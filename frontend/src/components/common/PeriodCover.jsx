import { coverTone } from "../../utils/coverTone";
import { formatMonth, formatNights } from "../../utils/date";
import styles from "./PeriodCover.module.css";

// 사진 대신 쓰는 여행 썸네일: 연한 색 바탕 + "3박 4일 / 9월"
// 색은 id로 골라서 같은 여행은 항상 같은 색이다. 경로 그림(RouteCover)을 그릴 좌표가 없을 때도 이걸 쓴다
export default function PeriodCover({ id, startDate, endDate, size = "sm" }) {
	return (
		<div className={`${styles.cover} ${styles[coverTone(id)]} ${styles[size]}`}>
			<span className={styles.nights}>{formatNights(startDate, endDate)}</span>
			<span className={styles.month}>{formatMonth(startDate)}</span>
		</div>
	);
}
