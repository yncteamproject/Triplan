package kr.ync.triplan.trip.repository;

import kr.ync.triplan.trip.domain.TransportSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TransportSegmentRepository extends JpaRepository<TransportSegment, Long> {

	List<TransportSegment> findByTripId(Long tripId);

	void deleteByTripId(Long tripId);

	void deleteByFromStopIdOrToStopId(Long fromStopId, Long toStopId);

	// 여러 여행의 교통비 합계를 한 번에 구한다 (비용이 비어 있으면 0으로 계산)
	@Query("select t.trip.id as tripId, coalesce(sum(t.cost), 0) as amount "
			+ "from TransportSegment t where t.trip.id in :tripIds group by t.trip.id")
	List<TripIdAmount> sumCostByTripIds(@Param("tripIds") Collection<Long> tripIds);
}
