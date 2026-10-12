import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { getErrorMessage } from "../../api/client";
import { createSharePage, getSharePage, getSharedTrip, updateSharePage } from "../../api/sharePageApi";
import { getTrips } from "../../api/tripApi";
import Button from "../../components/common/Button";
import EmptyState from "../../components/common/EmptyState";
import Input from "../../components/common/Input";
import Select from "../../components/common/Select";
import Textarea from "../../components/common/Textarea";
import { useAuth } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import { formatNights, formatPeriod } from "../../utils/date";
import styles from "./SharePageFormPage.module.css";

// 서버 검증과 같은 길이
const TITLE_MAX_LENGTH = 100;
const DESCRIPTION_MAX_LENGTH = 2000;

// 게시글 글쓰기(/share-pages/new) · 수정(/share-pages/:id/edit) (피그마 "📄 게시판 - 글쓰기 · 수정")
// 주소에 id가 있으면 수정이다. 필요한 값을 불러온 뒤에 폼(ShareForm)을 보여준다
export default function SharePageFormPage() {
	const { id } = useParams();
	const [searchParams] = useSearchParams();
	const { user } = useAuth();
	const isEdit = id !== undefined;
	const [reloadCount, setReloadCount] = useState(0); // "다시 시도"를 누르면 올려서 다시 불러온다
	const [result, setResult] = useState(null); // { key, status: ready | notFound | error, … }

	// 수정 주소의 id가 숫자가 아니면 서버에 묻지 않고 바로 "찾을 수 없어요"를 보여준다
	const validId = !isEdit || /^\d+$/.test(id);

	// 어떤 요청의 결과인지 구분하는 값. 결과의 key가 다르면 아직 불러오는 중이다
	const loadKey = `${isEdit ? id : "new"}:${reloadCount}`;

	useEffect(() => {
		if (!validId) {
			return undefined;
		}
		let cancelled = false;
		// 글쓰기: 고를 수 있는 내 여행 목록 / 수정: 기존 글과, 그 글에 연결된 여행(이름 · 기간 표시용)
		const request = isEdit
			? Promise.all([getSharePage(id), getSharedTrip(id)]).then(([post, trip]) => ({ post, trip }))
			: getTrips().then((trips) => ({ trips }));
		request
			.then((data) => {
				if (!cancelled) {
					setResult({ key: loadKey, status: "ready", ...data });
				}
			})
			.catch((err) => {
				if (cancelled) {
					return;
				}
				if (isEdit && err.response?.status === 404) {
					setResult({ key: loadKey, status: "notFound" });
				} else {
					setResult({ key: loadKey, status: "error", message: getErrorMessage(err, "불러오지 못했어요.") });
				}
			});
		// 화면을 떠난 뒤에 응답이 오면 무시한다
		return () => {
			cancelled = true;
		};
	}, [id, isEdit, loadKey, validId]);

	if (validId && result?.key !== loadKey) {
		return (
			<section className={styles.page}>
				<p className={styles.loading}>불러오는 중이에요…</p>
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

	// 수정은 작성자만 할 수 있다 (서버도 403으로 막는다, B3)
	if (isEdit && result.post.writerId !== user?.memberId) {
		return (
			<section className={styles.page}>
				<EmptyState
					title="수정할 수 없는 글이에요"
					description="내가 쓴 글만 수정할 수 있어요"
					action={<Button to={`/share-pages/${id}`}>글 보러 가기</Button>}
				/>
			</section>
		);
	}

	if (!isEdit && result.trips.length === 0) {
		return (
			<section className={styles.page}>
				<EmptyState
					title="먼저 여행 계획을 만들어주세요"
					description="공유할 여행 계획이 아직 없어요"
					action={<Button to="/trips/new">여행 만들기</Button>}
				/>
			</section>
		);
	}

	// 여행 상세의 "게시판에 공유"로 들어오면 주소에 ?tripId= 가 붙어 있다. 내 여행일 때만 미리 선택한다
	const requestedTripId = searchParams.get("tripId");
	const initialTripId =
		!isEdit && result.trips.some((trip) => String(trip.id) === requestedTripId) ? requestedTripId : "";

	return (
		<section className={styles.page}>
			{/* key: 다른 글 · 다른 요청의 결과면 폼을 새로 만든다 */}
			<ShareForm key={loadKey} post={result.post} trip={result.trip} trips={result.trips} initialTripId={initialTripId} />
		</section>
	);
}

// 폼 본체. post가 있으면 수정, 없으면 글쓰기
function ShareForm({ post, trip, trips, initialTripId }) {
	const navigate = useNavigate();
	const { showToast } = useToast();
	const isEdit = Boolean(post);
	const [tripId, setTripId] = useState(initialTripId);
	const [title, setTitle] = useState(post?.title ?? "");
	const [description, setDescription] = useState(post?.description ?? "");
	const [allowCopy, setAllowCopy] = useState(post?.allowCopy ?? true);
	const [error, setError] = useState("");
	const [submitting, setSubmitting] = useState(false);

	const handleSubmit = async (event) => {
		event.preventDefault();
		if (!isEdit && !tripId) {
			setError("여행 계획을 선택해주세요");
			return;
		}
		if (!title.trim()) {
			setError("제목을 입력해주세요");
			return;
		}
		setError("");
		setSubmitting(true);
		const content = { title: title.trim(), description: description.trim() || null, allowCopy };
		try {
			const saved = isEdit
				? await updateSharePage(post.id, content)
				: await createSharePage({ ...content, tripId: Number(tripId) });
			showToast(isEdit ? "게시글을 수정했어요" : "게시글을 등록했어요");
			navigate(`/share-pages/${saved.id}`, { replace: true });
		} catch (err) {
			setError(getErrorMessage(err, "저장하지 못했어요. 잠시 후 다시 시도해주세요."));
			setSubmitting(false);
		}
	};

	const change = (setter) => (event) => {
		setter(event.target.value);
		setError("");
	};

	return (
		<form className={styles.card} onSubmit={handleSubmit} noValidate>
			<h2 className={styles.title}>{isEdit ? "게시글 수정" : "여행 계획 공유하기"}</h2>

			{isEdit ? (
				// 공유한 여행은 바꿀 수 없어서 이름과 기간만 보여준다
				<div className={styles.fixedTrip}>
					<span className={styles.fixedTripLabel}>공유한 여행</span>
					<span className={styles.fixedTripName}>{trip.title}</span>
					<span className={styles.fixedTripPeriod}>
						{formatPeriod(trip.startDate, trip.endDate)} · {formatNights(trip.startDate, trip.endDate)}
					</span>
				</div>
			) : (
				<Select
					label="공유할 여행"
					placeholder="여행 계획 선택"
					options={trips.map((item) => ({
						value: String(item.id),
						label: `${item.title} (${formatPeriod(item.startDate, item.endDate)})`,
					}))}
					value={tripId}
					onChange={change(setTripId)}
				/>
			)}

			<Input
				variant="box"
				label="제목"
				placeholder="예: 제주도 2박 3일 알차게 다녀왔어요"
				maxLength={TITLE_MAX_LENGTH}
				value={title}
				onChange={change(setTitle)}
			/>
			<Textarea
				label="설명 (선택)"
				placeholder="어떤 여행이었는지, 추천하고 싶은 점을 적어보세요"
				rows={8}
				maxLength={DESCRIPTION_MAX_LENGTH}
				value={description}
				onChange={(event) => setDescription(event.target.value)}
				helper={`${description.length} / ${DESCRIPTION_MAX_LENGTH}`}
			/>

			<label className={styles.check}>
				<input type="checkbox" checked={allowCopy} onChange={(event) => setAllowCopy(event.target.checked)} />
				<span>
					<span className={styles.checkTitle}>복사 허용</span>
					<span className={styles.checkHelp}>다른 사람이 이 여행을 "내 여행으로 복사"할 수 있어요</span>
				</span>
			</label>

			<p className={styles.notice}>방문지 · 이동 구간 · 숙소 · 비용이 함께 공개돼요. 예약번호는 공유되지 않아요.</p>

			{error && <p className={styles.error}>{error}</p>}

			<div className={styles.actions}>
				<Button variant="secondary" to={isEdit ? `/share-pages/${post.id}` : "/share-pages"}>
					취소
				</Button>
				<Button type="submit" disabled={submitting}>
					{submitting ? "저장 중…" : isEdit ? "수정하기" : "등록하기"}
				</Button>
			</div>
		</form>
	);
}
