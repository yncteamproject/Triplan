package kr.ync.triplan.share.service;

import kr.ync.triplan.share.dto.request.SharePageCreateRequest;
import kr.ync.triplan.share.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.share.dto.request.TripCopyRequest;
import kr.ync.triplan.share.dto.response.SharePageListResponse;
import kr.ync.triplan.share.dto.response.SharePageResponse;
import kr.ync.triplan.share.dto.response.SharedTripResponse;
import kr.ync.triplan.trip.dto.response.TripResponse;

import java.util.List;

public interface SharePageService {

	SharePageResponse create(String email, SharePageCreateRequest request);

	List<SharePageListResponse> getList();

	SharePageResponse getDetail(Long id);

	SharedTripResponse getSharedTrip(Long id);

	SharePageResponse update(String email, Long id, SharePageUpdateRequest request);

	void delete(String email, Long id);

	TripResponse copyTrip(String email, Long id, TripCopyRequest request);
}
