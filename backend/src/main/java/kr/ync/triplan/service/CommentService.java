package kr.ync.triplan.service;

import kr.ync.triplan.dto.request.CommentCreateRequest;
import kr.ync.triplan.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {

	CommentResponse create(String email, Long sharePageId, CommentCreateRequest request);

	List<CommentResponse> getList(Long sharePageId);

	void delete(String email, Long commentId);
}
