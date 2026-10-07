import styles from "./Toast.module.css";

// 화면 위쪽 알림 한 줄. 직접 쓰지 말고 useToast().showToast("문구")로 띄운다
export default function Toast({ message }) {
	return (
		<div className={styles.toast} role="status">
			{message}
		</div>
	);
}
