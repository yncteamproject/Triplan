package kr.ync.triplan.trip.client;

import kr.ync.triplan.trip.exception.TransitApiException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Map;

// 오디세이 대중교통 길찾기 API를 호출해서 응답 JSON을 그대로 돌려준다 (해석은 OdsayResponseParser)
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
			throw new TransitApiException();
		}
	}
}
