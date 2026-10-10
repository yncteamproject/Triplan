// 12000 → "12,000원"
export const formatWon = (amount) => `${(amount ?? 0).toLocaleString("ko-KR")}원`;

// 비용 입력칸의 글자를 서버로 보낼 값으로. 빈칸은 null(비용 없음)
export const toCost = (text) => (text === "" ? null : Number(text));

// 비용 입력이 0 이상의 정수인지 (빈칸은 통과)
export const isValidCost = (text) => text === "" || /^\d+$/.test(text);
