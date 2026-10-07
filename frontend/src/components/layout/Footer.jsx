import { Link } from "react-router-dom";
import styles from "./Footer.module.css";

export default function Footer() {
	return (
		<footer className={styles.footer}>
			<div className={styles.top}>
				<div>
					<p className={styles.logo}>Triplan</p>
					<p className={styles.slogan}>여행을 계획하는 가장 쉬운 방법</p>
				</div>
				<div className={styles.column}>
					<p className={styles.heading}>서비스</p>
					<Link to="/trips">플래너</Link>
					<Link to="/travel-test">성향 테스트</Link>
					<Link to="/share-pages">커뮤니티</Link>
				</div>
				<div className={styles.column}>
					<p className={styles.heading}>팀</p>
					<a href="https://github.com/yncteamproject/Triplan" target="_blank" rel="noreferrer">
						GitHub
					</a>
				</div>
			</div>
			<p className={styles.copyright}>© 2026 YNC Team Project · Triplan</p>
		</footer>
	);
}
