const TONES = ["blue", "purple", "orange"];

// 썸네일의 색 이름. id로 골라서 같은 여행 · 같은 글은 항상 같은 색이다
// PeriodCover(기간 썸네일)와 RouteCover(경로 그림)가 같은 색을 쓰도록 한곳에 둔다
export const coverTone = (id) => TONES[Math.abs(Number(id) || 0) % TONES.length];
