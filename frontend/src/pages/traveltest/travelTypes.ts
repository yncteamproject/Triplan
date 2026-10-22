import type { TravelType } from "../../api/travelTestApi";

// 결과 화면에 쓰는 성향별 긴 설명 (피그마 문구). 색은 travelTypes.module.css
const travelTypes: Record<TravelType, string> = {
	FREE_EXPLORER: "계획보다는 발길 닿는 대로 움직이는 걸 즐기는 당신. 새로운 골목과 낯선 풍경에서 에너지를 얻는 타입이에요.",
	RELAXED_HEALER: "바쁜 일상에서 벗어나 조용히 쉬는 여행을 선호하는 당신. 한적한 자연 속에서 재충전할 때 가장 행복해요.",
	CULTURE_LOVER: "박물관, 갤러리, 오래된 골목에서 이야기를 발견하는 걸 좋아하는 당신. 그 지역의 역사와 예술을 깊이 느끼는 여행을 즐겨요.",
	FOOD_EXPLORER: "여행의 절반은 먹는 재미! 현지 맛집과 시장을 찾아다니며 새로운 맛을 경험하는 게 최고의 즐거움인 당신.",
};

export default travelTypes;