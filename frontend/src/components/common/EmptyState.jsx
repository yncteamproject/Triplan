import styles from "./EmptyState.module.css";

// 빈 화면 · 오류 화면 조각 (피그마 "상태 화면 모음"). action에는 <Button>을 넣는다
export default function EmptyState({ icon, title, description, action }) {
	return (
		<div className={styles.empty}>
			{icon && <div className={styles.icon}>{icon}</div>}
			<p className={styles.title}>{title}</p>
			{description && <p className={styles.description}>{description}</p>}
			{action && <div className={styles.action}>{action}</div>}
		</div>
	);
}
