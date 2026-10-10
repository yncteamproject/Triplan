import { useId } from "react";
import styles from "./Input.module.css";

// 공통 선택칸 (상자 모양). options: [{ value, label }], placeholder를 주면 맨 위에 "선택 안 함" 항목이 생긴다
export default function Select({ label, options, placeholder, error, className = "", ...rest }) {
	const id = useId();
	return (
		<div className={`${styles.field} ${className}`}>
			{label && (
				<label htmlFor={id} className={styles.label}>
					{label}
				</label>
			)}
			<select id={id} className={`${styles.input} ${styles.box} ${error ? styles.invalid : ""}`} {...rest}>
				{placeholder && <option value="">{placeholder}</option>}
				{options.map((option) => (
					<option key={option.value} value={option.value}>
						{option.label}
					</option>
				))}
			</select>
			{error && <p className={styles.error}>{error}</p>}
		</div>
	);
}
