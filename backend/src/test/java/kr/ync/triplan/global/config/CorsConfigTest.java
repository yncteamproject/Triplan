package kr.ync.triplan.global.config;

import kr.ync.triplan.support.BaseController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 화면 주소(CORS) 허용 목록을 설정값(app.cors.allowed-origins)으로 바꿀 수 있는지 확인 (배포)
// 배포 서버처럼 Vercel 주소를 넣었을 때, 그 주소는 API를 부를 수 있고 다른 주소는 막힌다
@TestPropertySource(properties = "app.cors.allowed-origins=https://triplan-test.vercel.app,http://localhost:5173")
class CorsConfigTest extends BaseController {

	private static final String VERCEL = "https://triplan-test.vercel.app";

	@Test
	@DisplayName("허용한 주소의 사전 요청 - 200, 허용 헤더가 붙음")
	void allowedOrigin_preflight() throws Exception {
		mockMvc.perform(options("/api/trips")
						.header(HttpHeaders.ORIGIN, VERCEL)
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, VERCEL));
	}

	@Test
	@DisplayName("목록의 두 번째 주소(localhost)도 허용")
	void secondOrigin_allowed() throws Exception {
		mockMvc.perform(get("/api/share-pages")
						.header(HttpHeaders.ORIGIN, "http://localhost:5173"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
	}

	@Test
	@DisplayName("허용하지 않은 주소 - 403, 허용 헤더 없음")
	void otherOrigin_forbidden() throws Exception {
		mockMvc.perform(options("/api/trips")
						.header(HttpHeaders.ORIGIN, "https://evil.example.com")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
	}
}
