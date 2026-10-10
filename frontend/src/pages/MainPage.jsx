import { useEffect, useState } from "react";
import { getSharePages } from "../api/sharePageApi";
import Button from "../components/common/Button";
import EmptyState from "../components/common/EmptyState";
import SharePageCard from "../components/share/SharePageCard";
import { useAuth } from "../context/AuthContext";
import styles from "./MainPage.module.css";

const LATEST_COUNT = 3; // "이런 여행은 어때요?"에 보여줄 글 수

const FEATURES = [
	{ title: "일정 계획", description: "방문지 · 이동 · 숙소를\n날짜별로 정리하기" },
	{ title: "자동 견적", description: "교통비와 숙박비를\n자동으로 합산" },
	{ title: "대중교통 경로", description: "방문지 사이 소요시간과\n요금을 한눈에" },
	{ title: "플래너 공유", description: "게시판에 공유하고\n마음에 드는 계획은 복사" },
];

// 메인 (피그마 "메인페이지"). 로그인 없이 볼 수 있다
export default function MainPage() {
	const { isLoggedIn } = useAuth();
	// 공유된 여행: 불러오는 중이면 null, 실패하면 "error", 성공하면 글 목록
	const [posts, setPosts] = useState(null);

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
			</section>

			<section className={styles.features}>
				<h2 className={styles.sectionTitle}>여행을 더 스마트하게</h2>
				<ul className={styles.featureList}>
					{FEATURES.map((feature, index) => (
						<li key={feature.title} className={styles.feature}>
							<span className={styles.featureIcon} aria-hidden="true">
								{index + 1}
							</span>
							<p className={styles.featureTitle}>{feature.title}</p>
							<p className={styles.featureDescription}>{feature.description}</p>
						</li>
					))}
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
