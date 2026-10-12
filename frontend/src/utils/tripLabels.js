// 이동 수단(서버의 TransportMode)의 화면 표시 이름
export const MODE_LABELS = { FLIGHT: "비행기", TRAIN: "기차", BUS: "버스", CAR: "자동차", WALK: "도보" };

export const MODE_OPTIONS = Object.entries(MODE_LABELS).map(([value, label]) => ({ value, label }));
