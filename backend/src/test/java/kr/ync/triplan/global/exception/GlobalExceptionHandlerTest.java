package kr.ync.triplan.global.exception;

import kr.ync.triplan.support.BaseController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 컨트롤러에서 따로 처리하지 않은 오류가 맞는 상태 코드와 ErrorResponse 형식으로 나가는지 확인 (K8)
class GlobalExceptionHandlerTest extends BaseController {

	@Test
	@DisplayName("숫자가 아닌 id - 400")
	void typeMismatch_badRequest() throws Exception {
		mockMvc.perform(get("/api/trips/abc")
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("요청 값의 형식이 올바르지 않습니다."));
	}

	@Test
	@DisplayName("깨진 JSON 본문 - 400")
	void brokenJson_badRequest() throws Exception {
		mockMvc.perform(post("/api/trips")
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\": \"제주"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("요청 본문의 형식이 올바르지 않습니다."));
	}

	@Test
	@DisplayName("날짜 형식이 틀린 본문 - 400")
	void invalidDateFormat_badRequest() throws Exception {
		mockMvc.perform(post("/api/trips")
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\": \"제주\", \"startDate\": \"내일\", \"endDate\": \"2026-10-12\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("요청 본문의 형식이 올바르지 않습니다."));
	}

	@Test
	@DisplayName("필수 쿼리 값 누락 - 400")
	void missingParameter_badRequest() throws Exception {
		mockMvc.perform(get("/api/trips/1/transit-routes")
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("필수 값이 없습니다: fromStopId"));
	}

	@Test
	@DisplayName("없는 주소 - 404")
	void unknownPath_notFound() throws Exception {
		mockMvc.perform(get("/api/no-such-path")
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("요청한 주소를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("지원하지 않는 메서드 - 405")
	void unsupportedMethod_methodNotAllowed() throws Exception {
		mockMvc.perform(delete("/api/trips")
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.message").value("지원하지 않는 요청 방식입니다."));
	}

	@Test
	@DisplayName("로그인 없이 요청 - 401 (기존 동작 그대로)")
	void withoutToken_unauthorized() throws Exception {
		mockMvc.perform(get("/api/trips/abc"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

	@Test
	@DisplayName("예상하지 못한 오류 - 500, 자세한 내용은 응답에 넣지 않음")
	void unexpectedError_internalServerError() throws Exception {
		// 일부러 오류를 내는 컨트롤러만 따로 띄워서 확인
		MockMvc standalone = MockMvcBuilders.standaloneSetup(new BrokenController())
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();

		standalone.perform(get("/broken"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.message").value("서버에서 오류가 발생했습니다."));
	}

	@RestController
	static class BrokenController {

		@GetMapping("/broken")
		String broken() {
			throw new IllegalStateException("DB 비밀번호 같은 내부 정보");
		}
	}
}
