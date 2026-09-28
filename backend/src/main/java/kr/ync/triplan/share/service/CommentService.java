package kr.ync.triplan.share.service;

import kr.ync.triplan.share.dto.request.CommentCreateRequest;
import kr.ync.triplan.share.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {

	CommentResponse create(String email, Long sharePageId, CommentCreateRequest request);

	List<CommentResponse> getList(Long sharePageId);

	void delete(String email, Long commentId);
}
