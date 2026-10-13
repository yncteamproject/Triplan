import { coverTone } from "../../utils/coverTone";
import styles from "./RouteCover.module.css";

// 그림의 크기 (SVG 안에서 쓰는 단위. 실제 크기는 카드 너비에 맞춰 늘어난다)
const WIDTH = 340;
const HEIGHT = 150;
const PADDING = 28; // 점이 가장자리에 붙지 않게 두는 여백
const MAX_NUMBERED = 8; // 점이 이보다 많으면 번호가 겹쳐서, 번호 없이 작은 점만 찍는다

// 위도 · 경도를 그림 안의 x · y로 바꾼다. 모든 점이 여백 안에 들어오게 맞추고 가운데에 둔다
function project(route) {
	// 경도 1도의 실제 거리는 위도가 높을수록 짧아진다. 그만큼 가로를 줄여야 모양이 찌그러지지 않는다
	const meanLatitude = route.reduce((sum, point) => sum + point.latitude, 0) / route.length;
	const shrink = Math.cos((meanLatitude * Math.PI) / 180);
	// 위도는 위로 갈수록 커지고 화면의 y는 아래로 갈수록 커지므로 세로를 뒤집는다
	const raw = route.map((point) => ({ x: point.longitude * shrink, y: -point.latitude }));

	const xs = raw.map((point) => point.x);
	const ys = raw.map((point) => point.y);
	const minX = Math.min(...xs);
	const minY = Math.min(...ys);
	const spanX = Math.max(...xs) - minX;
	const spanY = Math.max(...ys) - minY;

	// 가로 · 세로 중 더 빡빡한 쪽에 맞춘다. 한 줄로 늘어서서 폭이 0인 쪽은 계산에서 뺀다
	const scale = Math.min(
		spanX > 0 ? (WIDTH - PADDING * 2) / spanX : Infinity,
		spanY > 0 ? (HEIGHT - PADDING * 2) / spanY : Infinity,
	);
	// 모든 점이 같은 자리면(scale이 무한대) 한가운데에 모은다
	const fit = Number.isFinite(scale) ? scale : 0;
	const offsetX = (WIDTH - spanX * fit) / 2;
	const offsetY = (HEIGHT - spanY * fit) / 2;
	return raw.map((point) => ({ x: (point.x - minX) * fit + offsetX, y: (point.y - minY) * fit + offsetY }));
}

// 방문지 경로 그림 (S7 · FE-13): 방문지를 순서대로 선으로 잇고 번호 점을 찍는다
// 실제 지도가 아니라 좌표만으로 그리는 단순한 그림이다 (카드마다 지도를 띄우면 무거워서)
// route: [{ latitude, longitude }] (2개 이상), id: 색을 정하는 값
export default function RouteCover({ id, route }) {
	const points = project(route);
	const numbered = points.length <= MAX_NUMBERED;
	const radius = numbered ? 10 : 5;

	return (
		<div className={`${styles.cover} ${styles[coverTone(id)]}`}>
			<svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} className={styles.svg} role="img" aria-label={`방문지 ${route.length}곳을 이은 경로`}>
				{/* 지도 느낌을 내는 바탕의 길 모양 (꾸밈) */}
				<g className={styles.roads} aria-hidden="true">
					<path d={`M0 ${HEIGHT * 0.38} L${WIDTH} ${HEIGHT * 0.5}`} />
					<path d={`M${WIDTH * 0.3} 0 L${WIDTH * 0.22} ${HEIGHT}`} />
					<path d={`M${WIDTH * 0.68} 0 L${WIDTH * 0.74} ${HEIGHT}`} />
				</g>
				<polyline className={styles.line} points={points.map((point) => `${point.x},${point.y}`).join(" ")} />
				{points.map((point, index) => (
					// 순서가 바뀌지 않는 목록이라 순서를 key로 쓴다
					<g key={index}>
						<circle className={styles.dot} cx={point.x} cy={point.y} r={radius} />
						{numbered && (
							<text className={styles.number} x={point.x} y={point.y} dy="0.35em" textAnchor="middle">
								{index + 1}
							</text>
						)}
					</g>
				))}
			</svg>
		</div>
	);
}
