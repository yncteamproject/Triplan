import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
	deleteAdminComment,
	deleteAdminSharePage,
	getAdminComments,
	getAdminMembers,
	getAdminSharePages,
	getAdminStats,
} from "../../api/adminApi";
import { getErrorMessage } from "../../api/client";
import Button from "../../components/common/Button";
import EmptyState from "../../components/common/EmptyState";
import Modal from "../../components/common/Modal";
import Pagination from "../../components/common/Pagination";
import { useToast } from "../../context/ToastContext";
import styles from "./AdminPage.module.css";

const PAGE_SIZE = 10;

const TABS = [
	{ key: "sharePages", label: "게시글", load: getAdminSharePages },
	{ key: "comments", label: "댓글", load: getAdminComments },
	{ key: "members", label: "회원", load: getAdminMembers },
];

// "2026-10-15T13:05:12" → "2026.10.15 13:05"
const formatDateTime = (dateTime) => dateTime.slice(0, 16).replace("T", " ").replaceAll("-", ".");

export default function AdminPage() {
	const { showToast } = useToast();
	const [stats, setStats] = useState(null);
	const [forbidden, setForbidden] = useState(false);
	const [tab, setTab] = useState("sharePages");
	const [page, setPage] = useState(1);
	const [reloadCount, setReloadCount] = useState(0);
	const [list, setList] = useState(null); // { key, status: ready | error, … }
	const [target, setTarget] = useState(null); // 삭제 확인 창에 띄울 항목 { type, id, label }
	const [deleting, setDeleting] = useState(false);
	const [deleteError, setDeleteError] = useState("");

	// 결과의 key가 지금과 다르면 아직 불러오는 중
	const loadKey = `${tab}:${page}:${reloadCount}`;

	useEffect(() => {
		getAdminStats()
			.then(setStats)
			.catch((err) => {
				if (err.response?.status === 403) {
					setForbidden(true);
				}
			});
	}, [reloadCount]);

	useEffect(() => {
		let cancelled = false;
		const { load } = TABS.find((item) => item.key === tab);
		load(page - 1, PAGE_SIZE)
			.then((data) => {
				if (cancelled) {
					return;
				}
				// 마지막 페이지의 마지막 항목을 지웠으면 앞 페이지로
				if (data.content.length === 0 && page > 1) {
					setPage(page - 1);
					return;
				}
				setList({ key: loadKey, status: "ready", data });
			})
			.catch((err) => {
				if (!cancelled) {
					setList({ key: loadKey, status: "error", message: getErrorMessage(err, "목록을 불러오지 못했어요.") });
				}
			});
		return () => {
			cancelled = true;
		};
	}, [tab, page, loadKey]);

	const changeTab = (key) => {
		setTab(key);
		setPage(1);
	};

	const closeDialog = () => {
		if (!deleting) {
			setTarget(null);
			setDeleteError("");
		}
	};

	const handleDelete = async () => {
		setDeleting(true);
		setDeleteError("");
		try {
			if (target.type === "sharePage") {
				await deleteAdminSharePage(target.id);
			} else {
				await deleteAdminComment(target.id);
			}
		} catch (err) {
			setDeleteError(getErrorMessage(err, "삭제하지 못했어요. 잠시 후 다시 시도해주세요."));
			setDeleting(false);
			return;
		}
		setDeleting(false);
		setTarget(null);
		showToast("삭제했어요");
		setReloadCount((count) => count + 1);
	};

	if (forbidden) {
		return (
			<section className={styles.page}>
				<EmptyState
					title="관리자만 볼 수 있어요"
					description="관리자 계정으로 로그인해주세요"
					action={<Button to="/">홈으로</Button>}
				/>
			</section>
		);
	}

	return (
		<section className={styles.page}>
			<h1 className={styles.title}>관리자 페이지</h1>

			<div className={styles.stats}>
				<StatCard label="회원" value={stats?.memberCount} />
				<StatCard label="공유 게시글" value={stats?.sharePageCount} />
				<StatCard label="댓글" value={stats?.commentCount} />
			</div>

			<div className={styles.tabs} role="tablist">
				{TABS.map((item) => (
					<button
						key={item.key}
						type="button"
						role="tab"
						aria-selected={tab === item.key}
						className={tab === item.key ? `${styles.tab} ${styles.activeTab}` : styles.tab}
						onClick={() => changeTab(item.key)}
					>
						{item.label}
					</button>
				))}
			</div>

			{list?.key !== loadKey ? (
				<p className={styles.status}>불러오는 중이에요…</p>
			) : list.status === "error" ? (
				<EmptyState
					title={list.message}
					action={
						<Button variant="secondary" onClick={() => setReloadCount((count) => count + 1)}>
							다시 시도
						</Button>
					}
				/>
			) : list.data.content.length === 0 ? (
				<EmptyState title="아직 아무것도 없어요" />
			) : (
				<>
					<div className={styles.tableWrap}>
						{tab === "sharePages" && <SharePageTable rows={list.data.content} onDelete={setTarget} />}
						{tab === "comments" && <CommentTable rows={list.data.content} onDelete={setTarget} />}
						{tab === "members" && <MemberTable rows={list.data.content} />}
					</div>
					<Pagination page={page} totalPages={list.data.totalPages} onChange={setPage} />
				</>
			)}

			{target && (
				<Modal
					title={target.type === "sharePage" ? "게시글 삭제" : "댓글 삭제"}
					onClose={closeDialog}
					footer={
						<>
							<Button variant="secondary" onClick={closeDialog} disabled={deleting}>
								취소
							</Button>
							<Button variant="danger" onClick={handleDelete} disabled={deleting}>
								{deleting ? "삭제 중" : "삭제"}
							</Button>
						</>
					}
				>
					<p className={styles.dialogTarget}>{target.label}</p>
					<p>
						{target.type === "sharePage"
							? "이 게시글과 달린 댓글이 모두 지워져요. 되돌릴 수 없어요."
							: "이 댓글을 지울까요? 되돌릴 수 없어요."}
					</p>
					{deleteError && <p className={styles.error}>{deleteError}</p>}
				</Modal>
			)}
		</section>
	);
}

function StatCard({ label, value }) {
	return (
		<div className={styles.statCard}>
			<p className={styles.statLabel}>{label}</p>
			<p className={styles.statValue}>{value ?? "-"}</p>
		</div>
	);
}

function SharePageTable({ rows, onDelete }) {
	return (
		<table className={styles.table}>
			<thead>
				<tr>
					<th>제목</th>
					<th>작성자</th>
					<th>작성일</th>
					<th>조회</th>
					<th>복사</th>
					<th />
				</tr>
			</thead>
			<tbody>
				{rows.map((row) => (
					<tr key={row.id}>
						<td>
							<Link to={`/share-pages/${row.id}`} className={styles.link}>
								{row.title}
							</Link>
						</td>
						<td>
							{row.writerNickname}
							<span className={styles.sub}>{row.writerEmail}</span>
						</td>
						<td>{formatDateTime(row.writeDate)}</td>
						<td>{row.viewCount}</td>
						<td>{row.copyCount}</td>
						<td>
							<Button variant="danger" onClick={() => onDelete({ type: "sharePage", id: row.id, label: row.title })}>
								삭제
							</Button>
						</td>
					</tr>
				))}
			</tbody>
		</table>
	);
}

function CommentTable({ rows, onDelete }) {
	return (
		<table className={styles.table}>
			<thead>
				<tr>
					<th>내용</th>
					<th>작성자</th>
					<th>게시글</th>
					<th>작성일</th>
					<th />
				</tr>
			</thead>
			<tbody>
				{rows.map((row) => (
					<tr key={row.id}>
						<td className={styles.content}>{row.content}</td>
						<td>{row.writerNickname}</td>
						<td>
							<Link to={`/share-pages/${row.sharePageId}`} className={styles.link}>
								{row.sharePageTitle}
							</Link>
						</td>
						<td>{formatDateTime(row.createdAt)}</td>
						<td>
							<Button variant="danger" onClick={() => onDelete({ type: "comment", id: row.id, label: row.content })}>
								삭제
							</Button>
						</td>
					</tr>
				))}
			</tbody>
		</table>
	);
}

function MemberTable({ rows }) {
	return (
		<table className={styles.table}>
			<thead>
				<tr>
					<th>이메일</th>
					<th>닉네임</th>
					<th>구분</th>
					<th>가입일</th>
				</tr>
			</thead>
			<tbody>
				{rows.map((row) => (
					<tr key={row.id}>
						<td>{row.email}</td>
						<td>{row.nickname}</td>
						<td>
							<span className={row.role === "ADMIN" ? `${styles.role} ${styles.admin}` : styles.role}>
								{row.role === "ADMIN" ? "관리자" : "회원"}
							</span>
						</td>
						<td>{formatDateTime(row.createdAt)}</td>
					</tr>
				))}
			</tbody>
		</table>
	);
}
