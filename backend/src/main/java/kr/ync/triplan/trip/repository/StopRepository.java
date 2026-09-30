package kr.ync.triplan.trip.repository;

import kr.ync.triplan.trip.domain.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long> {

	List<Stop> findByTripIdOrderByStopOrderAsc(Long tripId);

	void deleteByTripId(Long tripId);
}
