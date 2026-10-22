import { useEffect, useState } from "react";
import { getErrorMessage } from "../../api/client";
import { getMyTravelResult, type TravelResult } from "../../api/travelTestApi";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import EmptyState from "../../components/common/EmptyState";
import travelTypes from "./travelTypes";
import typeStyles from "./travelTypes.module.css";
import styles from "./TravelTestResultPage.module.css";

export default function TravelTestResultPage() {
	const [result, setResult] = useState<TravelResult | null>();
	const [loadError, setLoadError] = useState("");

	useEffect(() => {
		getMyTravelResult()
			.then(setResult)
			.catch((err) => {
				// 테스트를 안 했으면 404
				if (err.response?.status === 404) {
					setResult(null);
				} else {
					setLoadError(getErrorMessage(err, "결과를 불러오지 못했어요."));
				}
			});
	}, []);

	if (result === undefined) {
		return (
			<section className={styles.wrap}>
				<p className={styles.status}>{loadError || "불러오는 중"}</p>
			</section>
		);
	}

	if (result === null) {
		return (
			<section className={styles.wrap}>
				<div className={styles.empty}>
					<EmptyState
						title="아직 여행 성향 테스트를 하지 않았어요"
						description="10개 질문으로 나에게 맞는 여행 스타일을 알아보세요"
						action={<Button to="/travel-test">테스트 하러 가기</Button>}
					/>
				</div>
			</section>
		);
	}

	return (
		<section className={styles.wrap}>
			<Card className={`${styles.card} ${typeStyles[result.travelType]}`}>
				<div className={styles.circle} />
				<p className={styles.caption}>당신의 여행 성향은</p>
				<h1 className={styles.name}>{result.displayName}</h1>
				<p className={styles.description}>{travelTypes[result.travelType] ?? result.description}</p>
				<Button to="/travel-test" size="lg" className={styles.retry}>
					테스트 다시 하기
				</Button>
			</Card>
		</section>
	);
}