package kr.ync.triplan.repository;

import kr.ync.triplan.domain.SharePage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharePageRepository extends JpaRepository<SharePage, Long> {

	List<SharePage> findAllByOrderByWriteDateDesc();

	List<SharePage> findByTripId(Long tripId);
}
