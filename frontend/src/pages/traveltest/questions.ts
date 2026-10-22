import type { TravelType } from "../../api/travelTestApi";

interface Question {
	title: string;
	options: { label: string; type: TravelType }[];
}

// 질문 10개. 선택지마다 성향 하나 (성향마다 5번씩 나옴)
const questions: Question[] = [
	{
		title: "여행 계획은 어떻게 세우나요?",
		options: [
			{ label: "큰 틀만 정하고 현지에서 즉흥으로", type: "FREE_EXPLORER" },
			{ label: "가고 싶은 맛집 목록부터 만든다", type: "FOOD_EXPLORER" },
		],
	},
	{
		title: "여행 첫날 가장 먼저 하고 싶은 일은?",
		options: [
			{ label: "그 지역 대표 박물관 가기", type: "CULTURE_LOVER" },
			{ label: "숙소에서 짐 풀고 푹 쉬기", type: "RELAXED_HEALER" },
		],
	},
	{
		title: "당신에게 완벽한 하루는?",
		options: [
			{ label: "도심 야경 산책", type: "FREE_EXPLORER" },
			{ label: "한적한 해변 노을", type: "RELAXED_HEALER" },
		],
	},
	{
		title: "여행지에서 꼭 사 오는 것은?",
		options: [
			{ label: "시장에서 파는 지역 특산물", type: "FOOD_EXPLORER" },
			{ label: "미술관 · 기념관 굿즈", type: "CULTURE_LOVER" },
		],
	},
	{
		title: "비 오는 날 여행지에서는?",
		options: [
			{ label: "카페 창가에서 빗소리 듣기", type: "RELAXED_HEALER" },
			{ label: "근처 전통시장 먹거리 투어", type: "FOOD_EXPLORER" },
		],
	},
	{
		title: "숙소를 고르는 기준은?",
		options: [
			{ label: "위치만 좋으면 OK, 바로 나가서 돌아다니기", type: "FREE_EXPLORER" },
			{ label: "한옥처럼 이야기가 있는 곳", type: "CULTURE_LOVER" },
		],
	},
	{
		title: "여행 사진첩에 가장 많은 사진은?",
		options: [
			{ label: "처음 가 본 골목 풍경", type: "FREE_EXPLORER" },
			{ label: "먹은 음식 사진", type: "FOOD_EXPLORER" },
		],
	},
	{
		title: "여행 중 하루가 비었다면?",
		options: [
			{ label: "온천이나 스파에서 쉬기", type: "RELAXED_HEALER" },
			{ label: "공연이나 전시 보러 가기", type: "CULTURE_LOVER" },
		],
	},
	{
		title: "여행지를 고르는 이유는?",
		options: [
			{ label: "아직 안 가 본 낯선 곳이라서", type: "FREE_EXPLORER" },
			{ label: "조용히 쉴 수 있는 곳이라서", type: "RELAXED_HEALER" },
		],
	},
	{
		title: "여행 마지막 날에는?",
		options: [
			{ label: "유명한 노포에서 마지막 한 끼", type: "FOOD_EXPLORER" },
			{ label: "역사 유적지 한 곳 더 둘러보기", type: "CULTURE_LOVER" },
		],
	},
];

export default questions;