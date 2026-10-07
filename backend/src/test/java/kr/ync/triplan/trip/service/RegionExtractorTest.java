package kr.ync.triplan.trip.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// 주소에서 시 · 도를 뽑는 규칙만 확인하는 단위 테스트 (스프링 · DB 없이 실행)
class RegionExtractorTest {

	@Test
	@DisplayName("특별자치도 · 특별시 · 광역시 · 도는 뒤를 떼고 짧게")
	void extract_fullNames() {
		assertThat(RegionExtractor.extract("제주특별자치도 제주시 공항로 2")).isEqualTo("제주");
		assertThat(RegionExtractor.extract("서울특별시 중구 세종대로 110")).isEqualTo("서울");
		assertThat(RegionExtractor.extract("부산광역시 해운대구 우동")).isEqualTo("부산");
		assertThat(RegionExtractor.extract("세종특별자치시 한누리대로 2130")).isEqualTo("세종");
		assertThat(RegionExtractor.extract("강원특별자치도 강릉시 창해로 17")).isEqualTo("강원");
		assertThat(RegionExtractor.extract("경기도 성남시 분당구")).isEqualTo("경기");
	}

	@Test
	@DisplayName("충청 · 전라 · 경상은 북 · 남을 살려서 두 글자로")
	void extract_northSouth() {
		assertThat(RegionExtractor.extract("충청북도 청주시")).isEqualTo("충북");
		assertThat(RegionExtractor.extract("충청남도 서산시")).isEqualTo("충남");
		assertThat(RegionExtractor.extract("전라북도 전주시")).isEqualTo("전북");
		assertThat(RegionExtractor.extract("전라남도 순천시")).isEqualTo("전남");
		assertThat(RegionExtractor.extract("경상북도 경주시")).isEqualTo("경북");
		assertThat(RegionExtractor.extract("경상남도 밀양시")).isEqualTo("경남");
	}

	@Test
	@DisplayName("이미 짧게 적힌 주소는 첫 단어 그대로")
	void extract_alreadyShort() {
		assertThat(RegionExtractor.extract("제주 서귀포시 성산읍")).isEqualTo("제주");
		assertThat(RegionExtractor.extract("서울 강남구 강남대로 396")).isEqualTo("서울");
		assertThat(RegionExtractor.extract("  경북   경주시 ")).isEqualTo("경북");
	}

	@Test
	@DisplayName("주소가 없거나 비어 있으면 null")
	void extract_blank() {
		assertThat(RegionExtractor.extract(null)).isNull();
		assertThat(RegionExtractor.extract("")).isNull();
		assertThat(RegionExtractor.extract("   ")).isNull();
	}
}
