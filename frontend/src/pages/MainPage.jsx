import { useEffect, useState } from "react";
import { getSharePages } from "../api/sharePageApi";
import heroImage from "../assets/main-hero.jpg";
import Button from "../components/common/Button";
import EmptyState from "../components/common/EmptyState";
import SharePageCard from "../components/share/SharePageCard";
import { useAuth } from "../context/AuthContext";
import styles from "./MainPage.module.css";

const LATEST_COUNT = 3; // "이런 여행은 어때요?"에 보여줄 글 수

// 기능 소개 카드. 누르면 그 카드가 옆으로 커지면서 자세한 설명(detail)이 나타난다
// tone: 카드의 포인트 색 (아이콘 동그라미 · 꾸밈 도형)
// icon: 동그라미 안에 그릴 선 그림(SVG path 목록, 24 × 24 기준)
const FEATURES = [
	{
		title: "일정 계획",
		tone: "blue",
		description: "방문지 · 이동 · 숙소를\n날짜별로 정리하기",
		detail:
			"여행 기간을 정하면 날짜별 Day 탭이 만들어져요. 날짜마다 방문지를 추가하고 순서와 시간을 정리할 수 있어요. 방문지 사이의 이동 구간과 그날 묵는 숙소도 한 화면에서 같이 봐요.",
		link: { to: "/trips", label: "플래너로 가기" },
		icon: ["M5 6h14a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1z", "M4 10h16", "M8 3v4", "M16 3v4", "M8 14h3", "M8 17h6"],
	},
	{
		title: "자동 견적",
		tone: "purple",
		description: "교통비와 숙박비를\n자동으로 합산",
		detail:
			"이동 구간과 숙소에 비용을 적어 두면 총 교통비, 총 숙박비, 총 경비가 자동으로 계산돼요. 일정을 추가하거나 고칠 때마다 바로 반영돼서 따로 계산할 필요가 없어요.",
		link: { to: "/trips", label: "플래너로 가기" },
		icon: ["M7 3h10a1 1 0 0 1 1 1v16a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z", "M9 7h6v3H9z", "M9 14h.01", "M12 14h.01", "M15 14h.01", "M9 17h.01", "M12 17h.01", "M15 17h.01"],
	},
	{
		title: "대중교통 경로",
		tone: "orange",
		description: "방문지 사이 소요시간과\n요금을 한눈에",
		detail:
			"방문지 두 곳을 고르면 지하철 · 버스 경로를 찾아 소요시간, 요금, 환승 횟수를 알려줘요. 지도와 함께 여행 상세 화면에 곧 추가될 예정이에요.",
		link: null,
		icon: ["M7 4h10a2 2 0 0 1 2 2v10a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V6a2 2 0 0 1 2-2z", "M5 11h14", "M8 14h.01", "M16 14h.01", "M8 17v3", "M16 17v3"],
	},
	{
		title: "플래너 공유",
		tone: "blue",
		description: "게시판에 공유하고\n마음에 드는 계획은 복사",
		detail:
			"내 여행 계획을 게시판에 올려 다른 여행자와 나눌 수 있어요. 마음에 드는 계획은 \"내 여행으로 복사\"로 가져와서 날짜만 바꿔 쓰면 돼요. 예약번호는 공유되지 않아요.",
		link: { to: "/share-pages", label: "게시판 구경하기" },
		icon: ["M18 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6z", "M6 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z", "M18 22a3 3 0 1 0 0-6 3 3 0 0 0 0 6z", "M8.6 13.5l6.8 4", "M15.4 6.5l-6.8 4"],
	},
];

// 메인 (피그마 "메인페이지"). 로그인 없이 볼 수 있다
export default function MainPage() {
	const { isLoggedIn } = useAuth();
	// 공유된 여행: 불러오는 중이면 null, 실패하면 "error", 성공하면 글 목록
	const [posts, setPosts] = useState(null);
	const [openFeature, setOpenFeature] = useState(null); // 설명을 펼친 기능 카드의 제목 (없으면 null)

	useEffect(() => {
		let cancelled = false;
		getSharePages(0, LATEST_COUNT)
			.then((data) => {
				if (!cancelled) {
					setPosts(data.content);
				}
			})
			// 실패해도 메인 화면 전체를 오류 화면으로 만들지 않는다. 이 부분만 숨긴다
			.catch(() => {
				if (!cancelled) {
					setPosts("error");
				}
			});
		return () => {
			cancelled = true;
		};
	}, []);

	return (
		<>
			<section className={styles.hero}>
				<span className={styles.badge}>TRIPLAN · 여행 플래너</span>
				<h1 className={styles.title}>
					당신의 다음 여행,
					<br />더 쉽고 즐겁게 계획하세요
				</h1>
				<p className={styles.subtitle}>일정 관리부터 경비 계산, 대중교통 경로까지 — 한 곳에서 끝내는 여행 플래너</p>
				<div className={styles.actions}>
					{/* 로그인 전에는 여행 만들기로 보낸다 (로그인 화면을 거쳐서 돌아온다) */}
					{isLoggedIn ? (
						<Button to="/trips" size="lg">
							내 여행 보기
						</Button>
					) : (
						<Button to="/trips/new" size="lg">
							무료로 시작하기
						</Button>
					)}
					<Button to="/share-pages" variant="secondary" size="lg">
						둘러보기
					</Button>
				</div>
				{/* 사진 3장을 이어 붙인 꾸밈용 그림이라 대체 글은 비워둔다 */}
				<img className={styles.heroImage} src={heroImage} alt="" width="1212" height="466" />
			</section>

			<section className={styles.features}>
				<h2 className={styles.sectionTitle}>여행을 더 스마트하게</h2>
				<ul className={styles.featureList}>
					{FEATURES.map((feature) => {
						const opened = feature.title === openFeature;
						return (
							<li
								key={feature.title}
								className={[styles.feature, styles[feature.tone], opened ? styles.featureOn : ""].filter(Boolean).join(" ")}
							>
								{/* 카드 오른쪽의 꾸밈 도형 (카드가 열리면 조금 움직인다) */}
								<span className={styles.shapes} aria-hidden="true">
									<span className={styles.shapeBig} />
									<span className={styles.shapeRing} />
									<span className={styles.shapeSquare} />
									<span className={styles.shapeDot} />
								</span>

								{/* 누르면 카드가 옆으로 커지면서 설명이 나타나고, 다시 누르면 닫힌다 */}
								<button
									type="button"
									className={styles.featureHead}
									aria-expanded={opened}
									onClick={() => setOpenFeature(opened ? null : feature.title)}
								>
									<span className={styles.featureIcon}>
										<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
											{feature.icon.map((path) => (
												<path key={path} d={path} />
											))}
										</svg>
									</span>
									<span className={styles.featureTitle}>{feature.title}</span>
									<span className={styles.featureDescription}>{feature.description}</span>
									<span className={styles.featureMore}>{opened ? "닫기" : "자세히 보기"}</span>
								</button>

								{/* 닫혀 있을 때는 높이 0으로 접혀 있다. inert: 접힌 동안 안의 버튼이 눌리거나 Tab으로 잡히지 않게 */}
								<div className={styles.featureDetail} inert={!opened}>
									<div className={styles.featureDetailInner}>
										<p className={styles.featureDetailText}>{feature.detail}</p>
										{feature.link && (
											<Button to={feature.link.to} variant="secondary">
												{feature.link.label}
											</Button>
										)}
									</div>
								</div>
							</li>
						);
					})}
				</ul>
			</section>

			{posts !== "error" && (
				<section className={styles.shared}>
					<h2 className={styles.sectionTitle}>이런 여행은 어때요?</h2>
					<p className={styles.sectionSubtitle}>다른 여행자들이 공유한 계획을 보고, 마음에 들면 내 여행으로 복사해보세요</p>
					{posts === null ? (
						<p className={styles.loading}>공유된 여행을 불러오는 중이에요…</p>
					) : posts.length === 0 ? (
						<EmptyState title="아직 공유된 여행이 없어요" description="첫 번째로 여행 계획을 공유해보세요" />
					) : (
						<>
							<ul className={styles.cards}>
								{posts.map((post) => (
									<li key={post.id}>
										<SharePageCard post={post} />
									</li>
								))}
							</ul>
							<Button to="/share-pages" variant="secondary">
								게시판에서 더 보기 →
							</Button>
						</>
					)}
				</section>
			)}
		</>
	);
}
