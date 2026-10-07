package kr.ync.triplan.trip.repository;

// 여행별 집계 결과 한 줄 (방문지 수, 비용 합계 등). 여러 여행을 한 번의 쿼리로 묶어서 셀 때 쓴다
public interface TripIdAmount {

	Long getTripId();

	Long getAmount();
}
