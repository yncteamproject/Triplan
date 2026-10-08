package kr.ync.triplan.trip.service;

import java.util.Map;

// 주소에서 시 · 도 이름을 짧게 뽑는다. 예: "제주특별자치도 제주시 …" → "제주", "서울 중구 …" → "서울"
public final class RegionExtractor {

	// 줄임말이 앞 글자만 떼어서는 안 되는 도 (충청북도 → 충북)
	private static final Map<String, String> SHORT_NAMES = Map.of(
			"충청북도", "충북",
			"충청남도", "충남",
			"전라북도", "전북",
			"전라남도", "전남",
			"경상북도", "경북",
			"경상남도", "경남"
	);

	// 긴 것부터 확인해야 "특별자치도"가 "도"로 잘못 잘리지 않는다
	private static final String[] SUFFIXES = {"특별자치도", "특별자치시", "특별시", "광역시", "도"};

	private RegionExtractor() {
	}

	// 주소가 비어 있으면 null
	public static String extract(String address) {
		if (address == null || address.isBlank()) {
			return null;
		}
		String first = address.trim().split("\\s+")[0];
		String shortName = SHORT_NAMES.get(first);
		if (shortName != null) {
			return shortName;
		}
		for (String suffix : SUFFIXES) {
			// "도" 한 글자만 남는 일을 막기 위해 떼고 나서 2글자 이상일 때만 뗀다
			if (first.endsWith(suffix) && first.length() - suffix.length() >= 2) {
				return first.substring(0, first.length() - suffix.length());
			}
		}
		return first;
	}
}
