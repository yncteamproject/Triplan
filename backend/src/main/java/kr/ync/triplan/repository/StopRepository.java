package kr.ync.triplan.repository;

import kr.ync.triplan.domain.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long> {

	List<Stop> findByTripIdOrderByStopOrderAsc(Long tripId);
}
