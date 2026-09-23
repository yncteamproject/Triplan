package kr.ync.triplan.service;

import kr.ync.triplan.dto.request.SharePageCreateRequest;
import kr.ync.triplan.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.dto.response.SharePageListResponse;
import kr.ync.triplan.dto.response.SharePageResponse;

import java.util.List;

public interface SharePageService {

	SharePageResponse create(String email, SharePageCreateRequest request);

	List<SharePageListResponse> getList();

	SharePageResponse getDetail(Long id);

	SharePageResponse update(String email, Long id, SharePageUpdateRequest request);

	void delete(String email, Long id);
}
