package kr.ync.triplan.share.repository;

import kr.ync.triplan.share.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findBySharePageIdOrderByCreatedAtAsc(Long sharePageId);

	void deleteBySharePageId(Long sharePageId);

	void deleteBySharePageTripId(Long tripId);
}
