import { Outlet } from "react-router-dom";
import Footer from "./Footer";
import Header from "./Header";
import styles from "./Layout.module.css";

// 모든 화면의 공통 틀: 헤더 + 화면 내용(Outlet) + 푸터
export default function Layout() {
	return (
		<div className={styles.layout}>
			<Header />
			<main className={styles.main}>
				<Outlet />
			</main>
			<Footer />
		</div>
	);
}
