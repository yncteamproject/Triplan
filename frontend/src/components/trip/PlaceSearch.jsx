import { useState } from "react";
import { searchPlaces } from "../../utils/kakao";
import Button from "../common/Button";
import styles from "./PlaceSearch.module.css";

// 장소 검색 (카카오 장소 검색). 결과에서 하나를 고르면 onSelect({ name, address, latitude, longitude })를 부른다
// 폼(<form>) 안에서 쓰기 때문에 Enter를 눌러도 바깥 폼이 제출되지 않게 막는다
export default function PlaceSearch({ onSelect }) {
	const [keyword, setKeyword] = useState("");
	const [status, setStatus] = useState("idle"); // idle | loading | done | error
	const [places, setPlaces] = useState([]);
	const [message, setMessage] = useState("");

	const search = async () => {
		const query = keyword.trim();
		if (!query) {
			return;
		}
		setStatus("loading");
		try {
			setPlaces(await searchPlaces(query));
			setStatus("done");
		} catch (error) {
			setMessage(error.message);
			setStatus("error");
		}
	};

	const handleKeyDown = (event) => {
		if (event.key === "Enter") {
			event.preventDefault();
			search();
		}
	};

	const choose = (place) => {
		onSelect(place);
		// 고른 뒤에는 결과를 접는다
		setPlaces([]);
		setStatus("idle");
		setKeyword("");
	};

	return (
		<div className={styles.search}>
			<label className={styles.label} htmlFor="place-search">
				장소 검색
			</label>
			<div className={styles.row}>
				<input
					id="place-search"
					className={styles.input}
					placeholder="예: 성산일출봉"
					value={keyword}
					onChange={(event) => setKeyword(event.target.value)}
					onKeyDown={handleKeyDown}
				/>
				<Button variant="secondary" onClick={search} disabled={status === "loading"}>
					{status === "loading" ? "검색 중…" : "검색"}
				</Button>
			</div>
			<p className={styles.helper}>검색해서 고르면 이름 · 주소 · 지도 위치가 채워져요</p>

			{status === "error" && <p className={styles.error}>{message}</p>}
			{status === "done" && places.length === 0 && (
				<p className={styles.empty}>검색 결과가 없어요. 다른 이름으로 검색하거나 아래에 직접 입력해주세요.</p>
			)}
			{places.length > 0 && (
				<ul className={styles.results}>
					{places.map((place) => (
						<li key={place.id}>
							<button type="button" className={styles.result} onClick={() => choose(place)}>
								<span className={styles.resultName}>
									{place.name}
									{place.category && <span className={styles.resultCategory}>{place.category}</span>}
								</span>
								<span className={styles.resultAddress}>{place.address}</span>
							</button>
						</li>
					))}
				</ul>
			)}
		</div>
	);
}
