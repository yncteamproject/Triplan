import { useEffect, useRef, useState } from "react";
import { loadKakao } from "../../utils/kakao";
import styles from "./TripMap.module.css";

// 방문지를 번호 마커와 선으로 보여주는 지도 (카카오맵, T6)
// points: 지도에 찍을 곳을 방문 순서대로 [{ label, name, latitude, longitude }]. label은 마커에 적을 번호
// 찍을 곳이 없으면 지도 대신 안내 문구를 보여준다
// emptyTitle · emptyHelp: 그 안내 문구. 보는 사람에 따라 바꿀 수 있다 (기본은 여행을 고치는 사람에게 하는 말)
export default function TripMap({
	points,
	emptyTitle = "방문지 위치를 넣으면 지도에 표시돼요",
	emptyHelp = '방문지를 추가하거나 수정할 때 "장소 검색"으로 고르면 위치가 들어가요',
}) {
	const containerRef = useRef(null);
	const mapRef = useRef(null); // 만들어 둔 지도 (탭을 바꿔도 다시 만들지 않는다)
	const drawnRef = useRef([]); // 지도에 올려 둔 마커 · 선 (다시 그릴 때 지운다)
	const [error, setError] = useState("");

	// 내용이 같으면 다시 그리지 않도록 글자로 바꿔서 비교한다
	const serialized = JSON.stringify(points);

	useEffect(() => {
		const list = JSON.parse(serialized);
		if (list.length === 0) {
			// 지도 칸이 화면에서 사라지므로 다음에 새로 만든다
			mapRef.current = null;
			drawnRef.current = [];
			return undefined;
		}
		let cancelled = false;
		loadKakao()
			.then((kakao) => {
				if (cancelled || !containerRef.current) {
					return;
				}
				const { maps } = kakao;
				const path = list.map((point) => new maps.LatLng(point.latitude, point.longitude));
				if (!mapRef.current) {
					mapRef.current = new maps.Map(containerRef.current, { center: path[0], level: 5 });
				}
				const map = mapRef.current;

				// 전에 그린 것을 지우고 다시 그린다
				drawnRef.current.forEach((item) => item.setMap(null));
				drawnRef.current = [];

				if (path.length > 1) {
					const line = new maps.Polyline({
						path,
						strokeWeight: 4,
						strokeColor: getComputedStyle(document.documentElement).getPropertyValue("--color-primary").trim(),
						strokeOpacity: 0.8,
					});
					line.setMap(map);
					drawnRef.current.push(line);
				}

				list.forEach((point, index) => {
					const marker = document.createElement("div");
					marker.className = styles.marker;
					marker.textContent = point.label;
					marker.title = point.name;
					const overlay = new maps.CustomOverlay({ position: path[index], content: marker, yAnchor: 0.5 });
					overlay.setMap(map);
					drawnRef.current.push(overlay);
				});

				// 모든 마커가 보이게 맞춘다. 한 곳뿐이면 그곳을 가운데에 둔다
				if (path.length === 1) {
					map.setLevel(4);
					map.setCenter(path[0]);
				} else {
					const bounds = new maps.LatLngBounds();
					path.forEach((position) => bounds.extend(position));
					map.setBounds(bounds, 32, 32, 32, 32);
				}
				setError("");
			})
			.catch((err) => {
				if (!cancelled) {
					setError(err.message);
				}
			});
		return () => {
			cancelled = true;
		};
	}, [serialized]);

	if (points.length === 0) {
		return (
			<div className={`${styles.map} ${styles.placeholder}`}>
				<p>{emptyTitle}</p>
				{emptyHelp && <p className={styles.placeholderHelp}>{emptyHelp}</p>}
			</div>
		);
	}

	return (
		<div className={styles.wrap}>
			<div ref={containerRef} className={styles.map} role="img" aria-label="방문지 지도" />
			{error && <p className={styles.error}>{error}</p>}
		</div>
	);
}
