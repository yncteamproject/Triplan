package kr.ync.triplan.share.repository;

import kr.ync.triplan.share.domain.SharePage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharePageRepository extends JpaRepository<SharePage, Long> {

	// 목록에 작성자 닉네임이 필요해서 작성자를 한 번에 같이 조회한다 (게시글마다 따로 조회하지 않도록)
	@Override
	@EntityGraph(attributePaths = "writer")
	Page<SharePage> findAll(Pageable pageable);

	List<SharePage> findByTripId(Long tripId);

	void deleteByTripId(Long tripId);
}
