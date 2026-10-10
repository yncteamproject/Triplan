import { Link } from "react-router-dom";
import { formatPeriod } from "../../utils/date";
import { formatWon } from "../../utils/format";
import PeriodCover from "../common/PeriodCover";
import styles from "./SharePageCard.module.css";

// 공유 게시글 카드. 게시판 목록과 메인 "이런 여행은 어때요?"가 같이 쓴다
// post: 목록 응답(GET /api/share-pages)의 게시글 하나. 누르면 게시글 상세로 간다
export default function SharePageCard({ post }) {
	return (
		<Link to={`/share-pages/${post.id}`} className={styles.card}>
			<div className={styles.cover}>
				<PeriodCover id={post.id} startDate={post.tripStartDate} endDate={post.tripEndDate} size="lg" />
				{post.copyCount > 0 && <span className={styles.copyBadge}>{post.copyCount}명이 복사</span>}
			</div>
			<div className={styles.body}>
				<p className={styles.name}>{post.title}</p>
				{/* 주소가 있는 방문지가 없으면 지역(region)이 없다. 그때는 기간만 */}
				<p className={styles.summary}>
					{post.region && `${post.region} · `}
					{formatPeriod(post.tripStartDate, post.tripEndDate)}
				</p>
				<p className={styles.numbers}>
					방문지 {post.stopCount}곳 · {formatWon(post.totalCost)}
				</p>
			</div>
		</Link>
	);
}
