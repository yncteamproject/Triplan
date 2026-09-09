package kr.ync.triplan.repository;

import kr.ync.triplan.domain.TransportSegment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportSegmentRepository extends JpaRepository<TransportSegment, Long> {

	List<TransportSegment> findByTripId(Long tripId);
}
