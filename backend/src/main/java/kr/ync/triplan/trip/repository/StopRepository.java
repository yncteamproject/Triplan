package kr.ync.triplan.trip.repository;

import kr.ync.triplan.trip.domain.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long> {

	List<Stop> findByTripIdOrderByStopOrderAsc(Long tripId);

	void deleteByTripId(Long tripId);

	// 여러 여행의 방문지 수를 한 번에 센다
	@Query("select s.trip.id as tripId, count(s) as amount from Stop s where s.trip.id in :tripIds group by s.trip.id")
	List<TripIdAmount> countByTripIds(@Param("tripIds") Collection<Long> tripIds);

	// 여러 여행에서 주소가 있는 방문지를 방문 순서대로 (여행의 지역을 구할 때 쓴다)
	List<Stop> findByTripIdInAndAddressIsNotNullOrderByStopOrderAsc(Collection<Long> tripIds);
}
