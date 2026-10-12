import { useEffect, useState } from "react";
import { getErrorMessage } from "../../api/client";
import { getTransitRoutes } from "../../api/tripApi";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import { formatDuration } from "../../utils/date";
import { formatWon } from "../../utils/format";
import styles from "./TransitRoutesModal.module.css";

const STEP_LABELS = { SUBWAY: "지하철", BUS: "버스", WALK: "도보" };

const hasLocation = (stop) => stop.latitude != null && stop.longitude != null;

// 두 방문지 사이의 대중교통 경로 (T7). 이 창을 열었을 때만 서버에 묻는다 (오디세이 무료 호출이 하루 30건이라서)
// from · to: 방문지. 둘 다 위치(위도 · 경도)가 있어야 한다
export default function TransitRoutesModal({ tripId, from, to, onClose }) {
	const located = hasLocation(from) && hasLocation(to);
	const [reloadCount, setReloadCount] = useState(0); // "다시 시도"를 누르면 올려서 다시 묻는다
	// status: loading | ready | error | noLocation
	const [result, setResult] = useState(located ? { status: "loading" } : { status: "noLocation" });

	useEffect(() => {
		// 위치가 없으면 서버가 400을 주므로, 묻지 않고 바로 안내한다
		if (!located) {
			return undefined;
		}
		let cancelled = false;
		getTransitRoutes(tripId, from.id, to.id)
			.then((data) => {
				if (!cancelled) {
					setResult({ status: "ready", routes: data.routes });
				}
			})
			.catch((err) => {
				if (!cancelled) {
					setResult({
						status: "error",
						message: getErrorMessage(err, "대중교통 정보를 불러오지 못했어요."),
						// 400은 요청이 잘못된 것(위치 없음 · 같은 방문지)이라 다시 시도해도 같다
						retryable: err.response?.status !== 400,
					});
				}
			});
		// 창을 닫은 뒤에 응답이 오면 무시한다
		return () => {
			cancelled = true;
		};
	}, [tripId, from.id, to.id, located, reloadCount]);

	const retry = () => {
		setResult({ status: "loading" });
		setReloadCount((count) => count + 1);
	};

	return (
		<Modal
			title="대중교통 경로"
			onClose={onClose}
			footer={
				<Button variant="secondary" onClick={onClose}>
					닫기
				</Button>
			}
		>
			<p className={styles.heading}>
				{from.name} → {to.name}
			</p>

			{result.status === "loading" && <p className={styles.message}>경로를 찾는 중이에요…</p>}

			{result.status === "noLocation" && (
				<p className={styles.message}>
					두 방문지 모두 위치가 있어야 경로를 찾을 수 있어요. 방문지를 수정해서 "장소 검색"으로 위치를 넣어주세요.
				</p>
			)}

			{result.status === "error" && (
				<div className={styles.messageBox}>
					<p className={styles.error}>{result.message}</p>
					{result.retryable && (
						<Button variant="secondary" onClick={retry}>
							다시 시도
						</Button>
					)}
				</div>
			)}

			{result.status === "ready" && result.routes.length === 0 && (
				<p className={styles.message}>대중교통 경로를 찾지 못했어요. 두 곳이 아주 가깝거나, 대중교통으로 갈 수 없는 구간일 수 있어요.</p>
			)}

			{result.status === "ready" && result.routes.length > 0 && (
				<ol className={styles.routes}>
					{result.routes.map((route, index) => (
						// 경로에 고유한 값이 없어서 순서를 key로 쓴다 (목록이 바뀌지 않는다)
						<li key={index} className={styles.route}>
							<p className={styles.routeHead}>
								<span className={styles.routeTime}>{formatDuration(route.totalTime)}</span>
								<span>{formatWon(route.payment)}</span>
								<span>{route.transferCount === 0 ? "환승 없음" : `환승 ${route.transferCount}회`}</span>
								<span>도보 {route.walkDistance.toLocaleString("ko-KR")}m</span>
							</p>
							<ol className={styles.steps}>
								{route.steps.map((step, stepIndex) => (
									<li key={stepIndex} className={`${styles.step} ${styles[step.type] ?? ""}`}>
										<span className={styles.stepType}>{STEP_LABELS[step.type] ?? step.type}</span>
										{step.type === "WALK" ? (
											<span>{formatDuration(step.time)}</span>
										) : (
											<span>
												{step.name} · {step.from} → {step.to} · {formatDuration(step.time)}
											</span>
										)}
									</li>
								))}
							</ol>
						</li>
					))}
				</ol>
			)}

			<p className={styles.source}>대중교통 정보: 오디세이(ODsay). 실제 운행과 다를 수 있어요.</p>
		</Modal>
	);
}
