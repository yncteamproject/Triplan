import styles from "./Pagination.module.css";

const WINDOW = 5; // 한 번에 보여줄 페이지 번호 개수

// 페이지 넘김. page는 1부터 센다 (서버는 0부터라서 화면에서 바꿔 보낸다)
// 예: <Pagination page={2} totalPages={7} onChange={(next) => …} />
export default function Pagination({ page, totalPages, onChange }) {
	if (totalPages <= 1) {
		return null;
	}
	// 지금 페이지가 가운데쯤 오도록 번호 묶음을 잡는다
	const start = Math.max(1, Math.min(page - Math.floor(WINDOW / 2), totalPages - WINDOW + 1));
	const numbers = Array.from({ length: Math.min(WINDOW, totalPages) }, (_, index) => start + index);

	return (
		<nav className={styles.pagination} aria-label="페이지">
			<button type="button" className={styles.button} disabled={page <= 1} onClick={() => onChange(page - 1)}>
				이전
			</button>
			{numbers.map((number) => (
				<button
					key={number}
					type="button"
					className={number === page ? `${styles.button} ${styles.current}` : styles.button}
					aria-current={number === page ? "page" : undefined}
					onClick={() => onChange(number)}
				>
					{number}
				</button>
			))}
			<button type="button" className={styles.button} disabled={page >= totalPages} onClick={() => onChange(page + 1)}>
				다음
			</button>
		</nav>
	);
}
