import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import Button from "../common/Button";
import styles from "./Header.module.css";

const MENUS = [
	{ to: "/", label: "홈", end: true },
	{ to: "/trips", label: "플래너" },
	{ to: "/travel-test", label: "성향 테스트" },
	{ to: "/share-pages", label: "커뮤니티" },
];

// 공통 헤더. 로그인 전: 로그인 · 회원가입 / 로그인 후: 닉네임님(마이페이지) · 로그아웃
export default function Header() {
	const { user, isLoggedIn, logout } = useAuth();
	const navigate = useNavigate();

	const handleLogout = () => {
		logout();
		navigate("/");
	};

	return (
		<header className={styles.header}>
			<Link to="/" className={styles.logo}>
				Triplan
			</Link>
			<nav className={styles.nav}>
				{MENUS.map((menu) => (
					<NavLink
						key={menu.to}
						to={menu.to}
						end={menu.end}
						className={({ isActive }) => (isActive ? `${styles.menu} ${styles.active}` : styles.menu)}
					>
						{menu.label}
					</NavLink>
				))}
			</nav>
			<div className={styles.actions}>
				{isLoggedIn ? (
					<>
						<Link to="/mypage" className={styles.nickname}>
							{user.nickname}님
						</Link>
						<Button variant="secondary" onClick={handleLogout}>
							로그아웃
						</Button>
					</>
				) : (
					<>
						<Link to="/login" className={styles.login}>
							로그인
						</Link>
						<Button to="/signup">회원가입</Button>
					</>
				)}
			</div>
		</header>
	);
}
