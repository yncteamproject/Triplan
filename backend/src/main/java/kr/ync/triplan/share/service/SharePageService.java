package kr.ync.triplan.share.service;

import kr.ync.triplan.global.dto.PageResponse;
import kr.ync.triplan.share.dto.request.SharePageCreateRequest;
import kr.ync.triplan.share.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.share.dto.request.TripCopyRequest;
import kr.ync.triplan.share.dto.response.SharePageListResponse;
import kr.ync.triplan.share.dto.response.SharePageResponse;
import kr.ync.triplan.share.dto.response.SharedTripResponse;
import kr.ync.triplan.trip.dto.response.TripResponse;

public interface SharePageService {

	SharePageResponse create(String email, SharePageCreateRequest request);

	PageResponse<SharePageListResponse> getList(int page, int size);

	SharePageResponse getDetail(Long id);

	SharedTripResponse getSharedTrip(Long id);

	SharePageResponse update(String email, Long id, SharePageUpdateRequest request);

	void delete(String email, Long id);

	TripResponse copyTrip(String email, Long id, TripCopyRequest request);
}
