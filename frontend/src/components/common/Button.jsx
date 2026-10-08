import { Link } from "react-router-dom";
import styles from "./Button.module.css";

// 공통 버튼. variant: primary(파랑) | secondary(흰색 + 테두리) | text(글자만) | danger(빨강, 삭제)
// to를 주면 링크로 동작한다. 예: <Button to="/signup">회원가입</Button>
export default function Button({
	variant = "primary",
	size = "md",
	fullWidth = false,
	to,
	className = "",
	children,
	...rest
}) {
	const classNames = [styles.button, styles[variant], styles[size], fullWidth ? styles.fullWidth : "", className]
		.filter(Boolean)
		.join(" ");

	if (to) {
		return (
			<Link to={to} className={classNames} {...rest}>
				{children}
			</Link>
		);
	}
	return (
		<button type="button" className={classNames} {...rest}>
			{children}
		</button>
	);
}
