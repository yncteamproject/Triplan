import styles from "./Card.module.css";

// 공통 카드. variant: form(로그인 스타일, 연회색) | panel(플래너 · 게시판, 흰색)
export default function Card({ variant = "panel", className = "", children, ...rest }) {
	return (
		<div className={`${styles.card} ${styles[variant]} ${className}`} {...rest}>
			{children}
		</div>
	);
}
