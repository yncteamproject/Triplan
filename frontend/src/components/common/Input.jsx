import { useId } from "react";
import styles from "./Input.module.css";

// 공통 입력칸. variant: underline(로그인 스타일, 밑줄) | box(플래너 · 게시판 폼, 상자)
// label · helper(안내) · error(오류 문구)는 선택. 나머지 속성은 <input>에 그대로 전달된다
export default function Input({ variant = "underline", label, helper, error, className = "", ...rest }) {
	const id = useId();
	return (
		<div className={`${styles.field} ${className}`}>
			{label && (
				<label htmlFor={id} className={styles.label}>
					{label}
				</label>
			)}
			<input id={id} className={`${styles.input} ${styles[variant]} ${error ? styles.invalid : ""}`} {...rest} />
			{error ? <p className={styles.error}>{error}</p> : helper && <p className={styles.helper}>{helper}</p>}
		</div>
	);
}
