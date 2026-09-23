package kr.ync.triplan.service;

import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.TripCreateRequest;
import kr.ync.triplan.dto.request.TripUpdateRequest;
import kr.ync.triplan.dto.response.TripResponse;
import kr.ync.triplan.exception.TripNotFoundException;
import kr.ync.triplan.repository.TripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class TripServiceImplTest {

	@Autowired
	private TripService tripService;

	@Autowired
	private TripRepository tripRepository;

	private static final long NON_EXISTING_ID = 99999L;

	private Trip savedTrip(String title, String userId) {
		return tripRepository.save(
				Trip.builder()
						.title(title)
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.userId(userId)
						.build()
		);
	}

	@Test
	@DisplayName("create")
	void create_success() {
		// given
		TripCreateRequest request = new TripCreateRequest(
				"제주도 여행", LocalDate.now(), LocalDate.now().plusDays(3), "user1");
		// when
		TripResponse response = tripService.create(request);
		// then
		assertThat(response.id()).isNotNull();
		assertThat(response)
				.extracting(TripResponse::title, TripResponse::userId)
				.containsExactly("제주도 여행", "user1");
	}

	@Test
	@DisplayName("getDetail")
	void getDetail_success() {
		// given
		Trip saved = savedTrip("제주도 여행", "user1");
		// when
		TripResponse response = tripService.getDetail(saved.getId());
		// then
		assertThat(response.id()).isEqualTo(saved.getId());
		assertThat(response.title()).isEqualTo("제주도 여행");
	}

	@Test
	@DisplayName("getDetail - 존재하지 않으면 예외")
	void getDetail_notFound() {
		assertThatThrownBy(() -> tripService.getDetail(NON_EXISTING_ID))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("list - 본인 여행만 조회")
	void list_success() {
		// given
		savedTrip("제주도 여행", "user1");
		savedTrip("부산 여행", "user1");
		savedTrip("서울 여행", "user2");
		// when
		List<TripResponse> list = tripService.getList("user1");
		// then
		assertThat(list).hasSize(2)
				.extracting(TripResponse::title)
				.containsExactlyInAnyOrder("제주도 여행", "부산 여행");
	}

	@Test
	@DisplayName("update")
	void update_success() {
		// given
		Trip saved = savedTrip("원래 제목", "user1");
		LocalDate newStart = LocalDate.now().plusDays(10);
		LocalDate newEnd = LocalDate.now().plusDays(15);
		// when
		TripResponse response = tripService.update(
				saved.getId(), new TripUpdateRequest("변경된 제목", newStart, newEnd));
		// then
		assertThat(response)
				.extracting(TripResponse::title, TripResponse::startDate, TripResponse::endDate)
				.containsExactly("변경된 제목", newStart, newEnd);
	}

	@Test
	@DisplayName("update - 존재하지 않으면 예외")
	void update_notFound() {
		TripUpdateRequest request = new TripUpdateRequest(
				"제목", LocalDate.now(), LocalDate.now().plusDays(1));
		assertThatThrownBy(() -> tripService.update(NON_EXISTING_ID, request))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("delete")
	void delete_success() {
		// given
		Trip saved = savedTrip("제목", "user1");
		// when
		tripService.delete(saved.getId());
		// then
		assertThatThrownBy(() -> tripService.getDetail(saved.getId()))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("delete - notFound")
	void delete_notFound() {
		assertThatThrownBy(() -> tripService.delete(NON_EXISTING_ID))
				.isInstanceOf(TripNotFoundException.class);
	}
}
