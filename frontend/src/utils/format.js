// 12000 → "12,000원"
export const formatWon = (amount) => `${(amount ?? 0).toLocaleString("ko-KR")}원`;
