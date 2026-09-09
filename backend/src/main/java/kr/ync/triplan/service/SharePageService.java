package kr.ync.triplan.service;

import kr.ync.triplan.dto.request.SharePageCreateRequest;
import kr.ync.triplan.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.dto.response.SharePageListResponse;
import kr.ync.triplan.dto.response.SharePageResponse;

import java.util.List;

public interface SharePageService {

	SharePageResponse create(SharePageCreateRequest request);

	List<SharePageListResponse> getList();

	SharePageResponse getDetail(Long id);

	SharePageResponse update(Long id, SharePageUpdateRequest request);

	void delete(Long id);
}
