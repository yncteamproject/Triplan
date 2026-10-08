import { useEffect } from "react";
import styles from "./Modal.module.css";

// 화면 가운데에 뜨는 창 (수정 폼 · 삭제 확인). 바깥을 누르거나 Esc를 누르면 onClose가 불린다
// footer에는 <Button>들을 넣는다
export default function Modal({ title, onClose, footer, children }) {
	useEffect(() => {
		const handleKey = (event) => {
			if (event.key === "Escape") {
				onClose();
			}
		};
		window.addEventListener("keydown", handleKey);
		return () => window.removeEventListener("keydown", handleKey);
	}, [onClose]);

	return (
		<div className={styles.overlay} onMouseDown={onClose}>
			<div
				className={styles.modal}
				role="dialog"
				aria-modal="true"
				aria-label={title}
				onMouseDown={(event) => event.stopPropagation()}
			>
				<h3 className={styles.title}>{title}</h3>
				<div className={styles.body}>{children}</div>
				{footer && <div className={styles.footer}>{footer}</div>}
			</div>
		</div>
	);
}
