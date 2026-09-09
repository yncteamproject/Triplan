package kr.ync.triplan.repository;

import kr.ync.triplan.domain.Lodging;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LodgingRepository extends JpaRepository<Lodging, Long> {

	List<Lodging> findByTripId(Long tripId);
}
