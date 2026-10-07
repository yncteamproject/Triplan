package kr.ync.triplan.trip.repository;

import kr.ync.triplan.trip.domain.Lodging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LodgingRepository extends JpaRepository<Lodging, Long> {

	List<Lodging> findByTripId(Long tripId);

	void deleteByTripId(Long tripId);

	// 여러 여행의 숙박비 합계를 한 번에 구한다 (비용이 비어 있으면 0으로 계산)
	@Query("select l.trip.id as tripId, coalesce(sum(l.cost), 0) as amount "
			+ "from Lodging l where l.trip.id in :tripIds group by l.trip.id")
	List<TripIdAmount> sumCostByTripIds(@Param("tripIds") Collection<Long> tripIds);
}
