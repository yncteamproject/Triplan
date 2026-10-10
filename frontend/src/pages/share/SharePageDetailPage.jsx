import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import { getErrorMessage } from "../../api/client";
import {
	copySharedTrip,
	createComment,
	deleteComment,
	deleteSharePage,
	getComments,
	getSharePage,
	getSharedTrip,
} from "../../api/sharePageApi";
import Button from "../../components/common/Button";
import EmptyState from "../../components/common/EmptyState";
import Input from "../../components/common/Input";
import Modal from "../../components/common/Modal";
import Textarea from "../../components/common/Textarea";
import TripSchedule from "../../components/trip/TripSchedule";
import { useAuth } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import { formatDateTime, formatNights, formatPeriod } from "../../utils/date";
import { buildSchedule } from "../../utils/schedule";
import styles from "./SharePageDetailPage.module.css";

const COMMENT_MAX_LENGTH = 500; // 서버 검증과 같은 길이

// 게시글 상세: 글 + 공유된 여행 보기 + 여행 복사 + 댓글 (피그마 "📄 게시판 - 상세 및 댓글")
// 보는 것은 로그인 없이 되고, 복사 · 댓글 쓰기는 로그인이 필요하다
export default function SharePageDetailPage() {
	const { id } = useParams();
	const navigate = useNavigate();
	const location = useLocation();
	const { user, isLoggedIn } = useAuth();
	const { showToast } = useToast();
	const [reloadCount, setReloadCount] = useState(0); // "다시 시도"를 누르면 올려서 다시 불러온다
	const [result, setResult] = useState(null); // { key, status: ready | notFound | error, … }
	const [selectedDay, setSelectedDay] = useState(null);
	const [dialog, setDialog] = useState(null); // 열린 확인 창 { kind: copy | deletePost | deleteComment, comment? }
	const [copyStartDate, setCopyStartDate] = useState("");
	const [working, setWorking] = useState(false); // 확인 창의 요청이 진행 중
	const [dialogError, setDialogError] = useState("");
	const [commentText, setCommentText] = useState("");
	const [commentError, setCommentError] = useState("");
	const [commentSubmitting, setCommentSubmitting] = useState(false);

	// 주소의 id가 숫자가 아니면 서버에 묻지 않고 바로 "찾을 수 없어요"를 보여준다
	const validId = /^\d+$/.test(id);

	// 어떤 요청의 결과인지 구분하는 값. 결과의 key가 다르면 아직 불러오는 중이다
	const loadKey = `${id}:${reloadCount}`;

	useEffect(() => {
		if (!validId) {
			return undefined;
		}
		let cancelled = false;
		Promise.all([getSharePage(id), getSharedTrip(id), getComments(id)])
			.then(([post, trip, comments]) => {
				if (!cancelled) {
					setResult({ key: loadKey, status: "ready", post, trip, comments });
				}
			})
			.catch((err) => {
				if (cancelled) {
					return;
				}
				if (err.response?.status === 404) {
					setResult({ key: loadKey, status: "notFound" });
				} else {
					setResult({
						key: loadKey,
						status: "error",
						message: getErrorMessage(err, "게시글을 불러오지 못했어요."),
					});
				}
			});
		// 화면을 떠난 뒤에 응답이 오면 무시한다
		return () => {
			cancelled = true;
		};
	}, [id, loadKey, validId]);

	if (validId && result?.key !== loadKey) {
		return (
			<section className={styles.page}>
				<p className={styles.loading}>게시글을 불러오는 중이에요…</p>
			</section>
		);
	}

	if (!validId || result.status === "notFound") {
		return (
			<section className={styles.page}>
				<EmptyState
					title="게시글을 찾을 수 없어요"
					description="삭제됐거나 없는 글이에요"
					action={<Button to="/share-pages">게시판 목록으로</Button>}
				/>
			</section>
		);
	}

	if (result.status === "error") {
		return (
			<section className={styles.page}>
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

	const { post, trip, comments } = result;
	const isMine = (writerId) => user?.memberId === writerId;
	// 복사를 막아 둔 글은 작성자 본인만 복사할 수 있다 (B11)
	const canCopy = post.allowCopy || isMine(post.writerId);
	const schedule = buildSchedule({ trip, stops: trip.stops, lodgings: trip.lodgings, selectedDay });

	// 로그인 화면으로 보낸다. 로그인하면 이 글로 돌아온다
	const goToLogin = () => navigate("/login", { state: { from: location } });

	const openDialog = (kind, comment) => {
		setDialog({ kind, comment });
		setDialogError("");
	};

	const closeDialog = () => {
		if (!working) {
			setDialog(null);
		}
	};

	const openCopy = () => {
		if (!isLoggedIn) {
			goToLogin();
			return;
		}
		setCopyStartDate("");
		openDialog("copy");
	};

	// 확인 창의 "확인" 버튼. 실패하면 창 안에 서버 문구를 보여준다
	const runDialog = async (action, fallback) => {
		setWorking(true);
		setDialogError("");
		try {
			await action();
		} catch (err) {
			setDialogError(getErrorMessage(err, fallback));
			setWorking(false);
			return false;
		}
		setWorking(false);
		setDialog(null);
		return true;
	};

	const handleCopy = () =>
		runDialog(async () => {
			const copied = await copySharedTrip(post.id, copyStartDate);
			showToast("내 여행으로 복사했어요");
			navigate(`/trips/${copied.id}`);
		}, "복사하지 못했어요. 잠시 후 다시 시도해주세요.");

	const handleDeletePost = () =>
		runDialog(async () => {
			await deleteSharePage(post.id);
			showToast("게시글을 삭제했어요");
			navigate("/share-pages", { replace: true });
		}, "삭제하지 못했어요. 잠시 후 다시 시도해주세요.");

	const handleDeleteComment = () =>
		runDialog(async () => {
			const { comment } = dialog;
			await deleteComment(comment.id);
			setResult((prev) => ({ ...prev, comments: prev.comments.filter((item) => item.id !== comment.id) }));
			showToast("댓글을 삭제했어요");
		}, "삭제하지 못했어요. 잠시 후 다시 시도해주세요.");

	const handleCommentSubmit = async (event) => {
		event.preventDefault();
		const content = commentText.trim();
		if (!content) {
			setCommentError("댓글 내용을 입력해주세요");
			return;
		}
		setCommentError("");
		setCommentSubmitting(true);
		try {
			const created = await createComment(post.id, content);
			setResult((prev) => ({ ...prev, comments: [...prev.comments, created] }));
			setCommentText("");
		} catch (err) {
			setCommentError(getErrorMessage(err, "댓글을 등록하지 못했어요. 잠시 후 다시 시도해주세요."));
		} finally {
			setCommentSubmitting(false);
		}
	};

	const dialogs = {
		copy: {
			title: "내 여행으로 복사할까요?",
			confirmLabel: "복사하기",
			workingLabel: "복사 중…",
			onConfirm: handleCopy,
			body: (
				<>
					<p>방문지 · 이동 구간 · 숙소가 내 여행으로 복사돼요. 비용은 그대로, 예약번호는 가져오지 않아요.</p>
					<Input
						variant="box"
						type="date"
						label="여행 시작일 (선택)"
						helper={`비워 두면 원본 날짜(${formatPeriod(trip.startDate, trip.endDate)}) 그대로 복사해요`}
						value={copyStartDate}
						onChange={(event) => setCopyStartDate(event.target.value)}
					/>
				</>
			),
		},
		deletePost: {
			title: "게시글을 삭제할까요?",
			confirmLabel: "삭제",
			workingLabel: "삭제 중…",
			danger: true,
			onConfirm: handleDeletePost,
			body: <p>댓글도 함께 삭제돼요. 공유한 여행 계획은 지워지지 않아요.</p>,
		},
		deleteComment: {
			title: "댓글을 삭제할까요?",
			confirmLabel: "삭제",
			workingLabel: "삭제 중…",
			danger: true,
			onConfirm: handleDeleteComment,
			body: <p>삭제하면 되돌릴 수 없어요.</p>,
		},
	};
	const openedDialog = dialog && dialogs[dialog.kind];

	return (
		<section className={styles.page}>
			<Link to="/share-pages" className={styles.back}>
				← 게시판 목록
			</Link>

			<header className={styles.head}>
				<div className={styles.headInfo}>
					<h2 className={styles.title}>{post.title}</h2>
					<p className={styles.meta}>
						{post.writerNickname} · {formatDateTime(post.writeDate)}
						{post.updateDate && " (수정됨)"} · 조회 {post.viewCount} · 복사 {post.copyCount}
					</p>
				</div>
				{isMine(post.writerId) && (
					<div className={styles.headActions}>
						<Button variant="secondary" to={`/share-pages/${post.id}/edit`}>
							수정
						</Button>
						<Button variant="text" className={styles.deleteButton} onClick={() => openDialog("deletePost")}>
							삭제
						</Button>
					</div>
				)}
			</header>

			{post.description && <p className={styles.description}>{post.description}</p>}

			<div className={styles.tripHead}>
				<div className={styles.headInfo}>
					<h3 className={styles.tripTitle}>{trip.title}</h3>
					<p className={styles.meta}>
						{formatPeriod(trip.startDate, trip.endDate)} · {formatNights(trip.startDate, trip.endDate)}
					</p>
				</div>
				{canCopy ? (
					<Button onClick={openCopy}>내 여행으로 복사</Button>
				) : (
					<span className={styles.noCopy}>복사를 허용하지 않은 글</span>
				)}
			</div>

			<TripSchedule
				schedule={schedule}
				segments={trip.transportSegments}
				lodgings={trip.lodgings}
				costs={trip}
				onSelectDay={setSelectedDay}
				emptyDescription="아직 방문지를 넣지 않은 여행이에요"
			/>

			<section className={styles.comments}>
				<h3 className={styles.commentsTitle}>댓글 {comments.length}</h3>
				{comments.length === 0 ? (
					<p className={styles.noComments}>첫 댓글을 남겨보세요</p>
				) : (
					<ul className={styles.commentList}>
						{comments.map((comment) => (
							<li key={comment.id} className={styles.comment}>
								<div className={styles.commentHead}>
									<span className={styles.commentWriter}>{comment.writerNickname}</span>
									<span className={styles.commentDate}>{formatDateTime(comment.createdAt)}</span>
									{isMine(comment.writerId) && (
										<button
											type="button"
											className={styles.commentDelete}
											onClick={() => openDialog("deleteComment", comment)}
										>
											삭제
										</button>
									)}
								</div>
								<p className={styles.commentContent}>{comment.content}</p>
							</li>
						))}
					</ul>
				)}

				{isLoggedIn ? (
					<form className={styles.commentForm} onSubmit={handleCommentSubmit} noValidate>
						<Textarea
							placeholder="댓글을 남겨보세요"
							aria-label="댓글 내용"
							maxLength={COMMENT_MAX_LENGTH}
							value={commentText}
							onChange={(event) => {
								setCommentText(event.target.value);
								setCommentError("");
							}}
							helper={`${commentText.length} / ${COMMENT_MAX_LENGTH}`}
							error={commentError}
						/>
						<Button type="submit" disabled={commentSubmitting}>
							{commentSubmitting ? "등록 중…" : "댓글 등록"}
						</Button>
					</form>
				) : (
					<div className={styles.commentLogin}>
						<span>댓글을 쓰려면 로그인이 필요해요</span>
						<Button variant="secondary" onClick={goToLogin}>
							로그인하고 댓글 쓰기
						</Button>
					</div>
				)}
			</section>

			{openedDialog && (
				<Modal
					title={openedDialog.title}
					onClose={closeDialog}
					footer={
						<>
							<Button variant="secondary" onClick={closeDialog} disabled={working}>
								취소
							</Button>
							<Button variant={openedDialog.danger ? "danger" : "primary"} onClick={openedDialog.onConfirm} disabled={working}>
								{working ? openedDialog.workingLabel : openedDialog.confirmLabel}
							</Button>
						</>
					}
				>
					{openedDialog.body}
					{dialogError && <p className={styles.error}>{dialogError}</p>}
				</Modal>
			)}
		</section>
	);
}
