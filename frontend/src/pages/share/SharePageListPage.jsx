import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { getErrorMessage } from "../../api/client";
import { getSharePages } from "../../api/sharePageApi";
import Button from "../../components/common/Button";
import EmptyState from "../../components/common/EmptyState";
import Pagination from "../../components/common/Pagination";
import SharePageCard from "../../components/share/SharePageCard";
import styles from "./SharePageListPage.module.css";

const PAGE_SIZE = 9; // 한 줄에 3개씩 세 줄

// 주소의 ?page= 값을 1부터 세는 페이지 번호로. 없거나 이상한 값이면 1
const toPage = (value) => (/^[1-9]\d*$/.test(value ?? "") ? Number(value) : 1);

// 게시판 목록 (피그마 "📄 게시판 - 목록"). 로그인 없이 볼 수 있다
export default function SharePageListPage() {
	const [searchParams, setSearchParams] = useSearchParams();
	const page = toPage(searchParams.get("page"));
	const [reloadCount, setReloadCount] = useState(0); // "다시 시도"를 누르면 올려서 다시 불러온다
	const [result, setResult] = useState(null); // { key, status: ready | error, … }

	// 어떤 요청의 결과인지 구분하는 값. 결과의 key가 다르면 아직 불러오는 중이다
	const loadKey = `${page}:${reloadCount}`;

	useEffect(() => {
		let cancelled = false;
		// 화면은 1부터, 서버는 0부터 센다
		getSharePages(page - 1, PAGE_SIZE)
			.then((data) => {
				if (cancelled) {
					return;
				}
				// 글은 있는데 없는 페이지 번호로 들어왔으면 1페이지로 보낸다
				if (data.content.length === 0 && data.totalElements > 0) {
					setSearchParams({}, { replace: true });
					return;
				}
				setResult({ key: loadKey, status: "ready", data });
			})
			.catch((err) => {
				if (!cancelled) {
					setResult({
						key: loadKey,
						status: "error",
						message: getErrorMessage(err, "게시글 목록을 불러오지 못했어요."),
					});
				}
			});
		// 화면을 떠나거나 페이지를 넘긴 뒤에 응답이 오면 무시한다
		return () => {
			cancelled = true;
		};
	}, [page, loadKey, setSearchParams]);

	const goToPage = (next) => {
		setSearchParams(next === 1 ? {} : { page: String(next) });
		window.scrollTo({ top: 0 });
	};

	const head = (
		<header className={styles.head}>
			<div>
				<h2 className={styles.title}>여행 계획 공유</h2>
				<p className={styles.subtitle}>다른 사람의 여행 계획을 구경하고 내 여행으로 가져와 보세요</p>
			</div>
			<Button to="/share-pages/new">글쓰기</Button>
		</header>
	);

	if (result?.key !== loadKey) {
		return (
			<section className={styles.page}>
				{head}
				<p className={styles.loading}>게시글을 불러오는 중이에요…</p>
			</section>
		);
	}

	if (result.status === "error") {
		return (
			<section className={styles.page}>
				{head}
				<EmptyState
					title={result.message}
					description="잠시 후 다시 시도해주세요"
					action={
						<Button variant="secondary" onClick={() => setReloadCount((count) => count + 1)}>
							다시 시도
						</Button>
					}
				/>
			</section>
		);
	}

	const { content, totalPages } = result.data;

	if (content.length === 0) {
		return (
			<section className={styles.page}>
				{head}
				<EmptyState
					title="아직 공유된 여행이 없어요"
					description="첫 번째로 여행 계획을 공유해보세요"
					action={<Button to="/share-pages/new">글쓰기</Button>}
				/>
			</section>
		);
	}

	return (
		<section className={styles.page}>
			{head}
			<ul className={styles.grid}>
				{content.map((post) => (
					<li key={post.id}>
						<SharePageCard post={post} />
					</li>
				))}
			</ul>
			<Pagination page={page} totalPages={totalPages} onChange={goToPage} />
		</section>
	);
}
