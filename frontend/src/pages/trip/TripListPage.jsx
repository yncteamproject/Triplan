import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getErrorMessage } from "../../api/client";
import { getTrips } from "../../api/tripApi";
import Button from "../../components/common/Button";
import EmptyState from "../../components/common/EmptyState";
import PeriodCover from "../../components/common/PeriodCover";
import { formatNights, formatPeriod } from "../../utils/date";
import styles from "./TripListPage.module.css";

// 내 여행 목록 (피그마 "📄 플래너 - 내 여행 목록", 없으면 "planner 페이지 - 플래너 없는 경우")
export default function TripListPage() {
	const [trips, setTrips] = useState([]);
	const [status, setStatus] = useState("loading"); // loading | ready | error
	const [errorMessage, setErrorMessage] = useState("");
	const [reloadCount, setReloadCount] = useState(0); // "다시 시도"를 누르면 올려서 다시 불러온다

	useEffect(() => {
		let cancelled = false;
		getTrips()
			.then((data) => {
				if (!cancelled) {
					setTrips(data);
					setStatus("ready");
				}
			})
			.catch((err) => {
				if (!cancelled) {
					setErrorMessage(getErrorMessage(err, "여행 목록을 불러오지 못했어요."));
					setStatus("error");
				}
			});
		// 화면을 떠난 뒤에 응답이 오면 무시한다
		return () => {
			cancelled = true;
		};
	}, [reloadCount]);

	const retry = () => {
		setStatus("loading");
		setReloadCount((count) => count + 1);
	};

	if (status === "loading") {
		return (
			<section className={styles.page}>
				<p className={styles.loading}>여행 목록을 불러오는 중이에요…</p>
			</section>
		);
	}

	if (status === "error") {
		return (
			<section className={styles.page}>
				<EmptyState
					title={errorMessage}
					description="잠시 후 다시 시도해주세요"
					action={
						<Button variant="secondary" onClick={retry}>
							다시 시도
						</Button>
					}
				/>
			</section>
		);
	}

	if (trips.length === 0) {
		return (
			<section className={styles.blank}>
				<h2 className={styles.blankTitle}>아직 만들어진 여행 계획이 없어요!</h2>
				<p className={styles.blankDescription}>새 여행 계획을 만들고 일정을 관리해보세요</p>
				<Button to="/trips/new" size="lg">
					플래너 생성하기
				</Button>
			</section>
		);
	}

	return (
		<section className={styles.page}>
			<header className={styles.head}>
				<div>
					<h2 className={styles.title}>내 여행 계획</h2>
					<p className={styles.subtitle}>여행을 만들고 일정을 채워보세요</p>
				</div>
				<Button to="/trips/new">+ 새 여행 만들기</Button>
			</header>
			<ul className={styles.list}>
				{trips.map((trip) => (
					<li key={trip.id}>
						<Link to={`/trips/${trip.id}`} className={styles.item}>
							<div className={styles.info}>
								<p className={styles.name}>{trip.title}</p>
								<p className={styles.meta}>
									{formatPeriod(trip.startDate, trip.endDate)} · {formatNights(trip.startDate, trip.endDate)}
								</p>
							</div>
							<PeriodCover id={trip.id} startDate={trip.startDate} endDate={trip.endDate} />
						</Link>
					</li>
				))}
			</ul>
		</section>
	);
}
