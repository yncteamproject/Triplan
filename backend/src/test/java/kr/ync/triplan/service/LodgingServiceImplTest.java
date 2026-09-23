package kr.ync.triplan.service;

import kr.ync.triplan.domain.Lodging;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.LodgingCreateRequest;
import kr.ync.triplan.dto.request.LodgingUpdateRequest;
import kr.ync.triplan.dto.response.LodgingResponse;
import kr.ync.triplan.exception.LodgingNotFoundException;
import kr.ync.triplan.exception.TripNotFoundException;
import kr.ync.triplan.repository.LodgingRepository;
import kr.ync.triplan.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class LodgingServiceImplTest {

	@Autowired
	private LodgingService lodgingService;

	@Autowired
	private LodgingRepository lodgingRepository;

	@Autowired
	private TripRepository tripRepository;

	private static final long NON_EXISTING_ID = 99999L;

	private Trip trip;

	@BeforeEach
	void setUpFixture() {
		trip = tripRepository.save(
				Trip.builder()
						.title("제주도 여행")
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.userId("user1")
						.build()
		);
	}

	private Lodging savedLodging(String name) {
		return lodgingRepository.save(
				Lodging.builder()
						.trip(trip)
						.name(name)
						.checkIn(LocalDateTime.now())
						.checkOut(LocalDateTime.now().plusDays(1))
						.build()
		);
	}

	@Test
	@DisplayName("create")
	void create_success() {
		// given
		LodgingCreateRequest request = new LodgingCreateRequest(
				"제주 호텔", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 100000, "RES123");
		// when
		LodgingResponse response = lodgingService.create(trip.getId(), request);
		// then
		assertThat(response.id()).isNotNull();
		assertThat(response)
				.extracting(LodgingResponse::name, LodgingResponse::tripId, LodgingResponse::cost)
				.containsExactly("제주 호텔", trip.getId(), 100000);
	}

	@Test
	@DisplayName("create - 존재하지 않는 여행이면 예외")
	void create_tripNotFound() {
		LodgingCreateRequest request = new LodgingCreateRequest(
				"제주 호텔", LocalDateTime.now(), LocalDateTime.now().plusDays(1), null, null);

		assertThatThrownBy(() -> lodgingService.create(NON_EXISTING_ID, request))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("getDetail")
	void getDetail_success() {
		// given
		Lodging saved = savedLodging("제주 호텔");
		// when
		LodgingResponse response = lodgingService.getDetail(saved.getId());
		// then
		assertThat(response.id()).isEqualTo(saved.getId());
		assertThat(response.name()).isEqualTo("제주 호텔");
	}

	@Test
	@DisplayName("getDetail - 존재하지 않으면 예외")
	void getDetail_notFound() {
		assertThatThrownBy(() -> lodgingService.getDetail(NON_EXISTING_ID))
				.isInstanceOf(LodgingNotFoundException.class);
	}

	@Test
	@DisplayName("list")
	void list_success() {
		// given
		savedLodging("제주 호텔");
		savedLodging("서귀포 펜션");
		// when
		List<LodgingResponse> list = lodgingService.getList(trip.getId());
		// then
		assertThat(list).hasSize(2)
				.extracting(LodgingResponse::name)
				.containsExactlyInAnyOrder("제주 호텔", "서귀포 펜션");
	}

	@Test
	@DisplayName("update")
	void update_success() {
		// given
		Lodging saved = savedLodging("원래 이름");
		// when
		LodgingResponse response = lodgingService.update(
				saved.getId(),
				new LodgingUpdateRequest("변경된 이름", LocalDateTime.now(), LocalDateTime.now().plusDays(2), 200000, "RES999"));
		// then
		assertThat(response)
				.extracting(LodgingResponse::name, LodgingResponse::cost)
				.containsExactly("변경된 이름", 200000);
	}

	@Test
	@DisplayName("update - 존재하지 않으면 예외")
	void update_notFound() {
		LodgingUpdateRequest request = new LodgingUpdateRequest(
				"이름", LocalDateTime.now(), LocalDateTime.now().plusDays(1), null, null);
		assertThatThrownBy(() -> lodgingService.update(NON_EXISTING_ID, request))
				.isInstanceOf(LodgingNotFoundException.class);
	}

	@Test
	@DisplayName("delete")
	void delete_success() {
		// given
		Lodging saved = savedLodging("제주 호텔");
		// when
		lodgingService.delete(saved.getId());
		// then
		assertThatThrownBy(() -> lodgingService.getDetail(saved.getId()))
				.isInstanceOf(LodgingNotFoundException.class);
	}

	@Test
	@DisplayName("delete - notFound")
	void delete_notFound() {
		assertThatThrownBy(() -> lodgingService.delete(NON_EXISTING_ID))
				.isInstanceOf(LodgingNotFoundException.class);
	}
}
