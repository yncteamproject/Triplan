package kr.ync.triplan.share.repository;

import kr.ync.triplan.share.domain.SharePage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharePageRepository extends JpaRepository<SharePage, Long> {

	List<SharePage> findAllByOrderByWriteDateDesc();

	List<SharePage> findByTripId(Long tripId);
}
