package kr.ync.triplan.trip.repository;

import kr.ync.triplan.trip.domain.Lodging;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LodgingRepository extends JpaRepository<Lodging, Long> {

	List<Lodging> findByTripId(Long tripId);

	void deleteByTripId(Long tripId);
}
