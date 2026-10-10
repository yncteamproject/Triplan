import { useId } from "react";
import styles from "./Input.module.css";

// 공통 여러 줄 입력칸 (상자 모양). label · helper · error는 선택
export default function Textarea({ label, helper, error, className = "", rows = 3, ...rest }) {
	const id = useId();
	return (
		<div className={`${styles.field} ${className}`}>
			{label && (
				<label htmlFor={id} className={styles.label}>
					{label}
				</label>
			)}
			<textarea
				id={id}
				rows={rows}
				className={`${styles.input} ${styles.box} ${styles.textarea} ${error ? styles.invalid : ""}`}
				{...rest}
			/>
			{error ? <p className={styles.error}>{error}</p> : helper && <p className={styles.helper}>{helper}</p>}
		</div>
	);
}
