// 카카오맵 SDK(지도 · 장소 검색)를 한 번만 불러온다
// 키는 frontend/.env의 VITE_KAKAO_MAP_KEY (JavaScript 키). 화면에서 쓰는 키라 공개돼도 되지만,
// Kakao Developers의 Web 플랫폼에 등록한 주소(http://localhost:5173 등)에서만 동작한다

const SDK_URL = "https://dapi.kakao.com/v2/maps/sdk.js";

let loading = null; // 불러오는 중이거나 끝난 Promise. 여러 곳에서 불러도 스크립트는 한 번만 넣는다

// 불러오기가 끝나면 window.kakao를 돌려준다. 실패하면 사용자에게 보여줄 문구가 담긴 Error
export function loadKakao() {
	if (loading) {
		return loading;
	}
	loading = new Promise((resolve, reject) => {
		const key = import.meta.env.VITE_KAKAO_MAP_KEY;
		if (!key) {
			reject(new Error("카카오맵 키가 없어요. frontend/.env의 VITE_KAKAO_MAP_KEY를 확인해주세요."));
			return;
		}
		const script = document.createElement("script");
		// autoload=false: 스크립트를 받은 뒤 kakao.maps.load()로 직접 준비시킨다. services: 장소 검색
		script.src = `${SDK_URL}?appkey=${key}&autoload=false&libraries=services`;
		script.async = true;
		script.onload = () => window.kakao.maps.load(() => resolve(window.kakao));
		script.onerror = () =>
			reject(new Error("카카오맵을 불러오지 못했어요. 인터넷 연결과 Kakao Developers의 Web 플랫폼 등록을 확인해주세요."));
		document.head.appendChild(script);
	}).catch((error) => {
		loading = null; // 실패하면 다음에 다시 시도할 수 있게 비운다
		throw error;
	});
	return loading;
}

// 장소를 이름으로 검색한다. 결과: [{ id, name, address, category, latitude, longitude }] (없으면 빈 목록)
export async function searchPlaces(keyword) {
	const kakao = await loadKakao();
	const { Status } = kakao.maps.services;
	return new Promise((resolve, reject) => {
		new kakao.maps.services.Places().keywordSearch(keyword, (data, status) => {
			if (status === Status.OK) {
				resolve(
					data.map((place) => ({
						id: place.id,
						name: place.place_name,
						// 도로명 주소가 없는 곳은 지번 주소를 쓴다
						address: place.road_address_name || place.address_name,
						category: place.category_group_name,
						latitude: Number(place.y),
						longitude: Number(place.x),
					})),
				);
			} else if (status === Status.ZERO_RESULT) {
				resolve([]);
			} else {
				reject(new Error("장소를 검색하지 못했어요. 잠시 후 다시 시도해주세요."));
			}
		});
	});
}
