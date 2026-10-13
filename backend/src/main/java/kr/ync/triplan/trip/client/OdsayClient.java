package kr.ync.triplan.trip.client;

import kr.ync.triplan.trip.exception.TransitApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

// 오디세이 대중교통 길찾기 API를 호출해서 응답 JSON을 그대로 돌려준다 (해석은 OdsayResponseParser)
// 실패하면 사용자에게는 502만 보내고, 이유는 서버 로그에 남긴다 (K11). 로그에 API 키는 남기지 않는다
@Slf4j
@Component
public class OdsayClient {

	private final OdsayProperties properties;
	private final RestClient restClient;

	public OdsayClient(OdsayProperties properties) {
		this.properties = properties;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(3));
		// 실제로 재보니 첫 조회가 4~5초 걸려서, 응답 대기는 넉넉히 10초
		requestFactory.setReadTimeout(Duration.ofSeconds(10));
		this.restClient = RestClient.builder()
				.baseUrl(properties.getBaseUrl())
				.requestFactory(requestFactory)
				.build();
	}

	// 좌표는 경도(x), 위도(y) 순서
	public String searchPubTransPath(double startX, double startY, double endX, double endY) {
		if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
			log.warn("오디세이 호출 실패 - 키가 설정되지 않았습니다. application-secret.yaml의 odsay.api-key를 확인하세요.");
			throw new TransitApiException();
		}
		try {
			// 키에 +, /, = 가 들어 있어도 깨지지 않도록 모든 값을 URI 변수로 넘겨서 인코딩한다
			return restClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/searchPubTransPathT")
							.queryParam("SX", "{sx}")
							.queryParam("SY", "{sy}")
							.queryParam("EX", "{ex}")
							.queryParam("EY", "{ey}")
							.queryParam("apiKey", "{apiKey}")
							.build(Map.of(
									"sx", startX, "sy", startY, "ex", endX, "ey", endY,
									"apiKey", properties.getApiKey())))
					.retrieve()
					.body(String.class);
		} catch (RestClientException e) {
			// 연결 실패 · 시간 초과 · 4xx/5xx 응답 등. 예외 문구에 요청 주소가 들어갈 수 있어서 키를 가린 뒤 남긴다
			log.warn("오디세이 호출 실패 - {}: {}", e.getClass().getSimpleName(), maskApiKey(e.getMessage()));
			throw new TransitApiException();
		}
	}

	// 문구 안에 API 키가 그대로 또는 주소용으로 바뀐 모양(+ → %2B 등)으로 들어 있으면 ***로 가린다
	private String maskApiKey(String message) {
		if (message == null) {
			return "";
		}
		String apiKey = properties.getApiKey();
		return message
				.replace(apiKey, "***")
				.replace(URLEncoder.encode(apiKey, StandardCharsets.UTF_8), "***");
	}
}
