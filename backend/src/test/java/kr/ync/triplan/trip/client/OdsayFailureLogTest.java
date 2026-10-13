package kr.ync.triplan.trip.client;

import kr.ync.triplan.trip.exception.TransitApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import tools.jackson.databind.json.JsonMapper;

import static kr.ync.triplan.support.OdsayFixture.AUTH_FAILED_JSON;
import static kr.ync.triplan.support.OdsayFixture.TOO_CLOSE_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 오디세이 호출이 실패했을 때 이유가 서버 로그에 남는지 확인 (K11). 스프링 · DB 없이 실행
// 사용자에게 가는 응답(502)은 그대로이고, 로그에 API 키가 들어가지 않아야 한다
@ExtendWith(OutputCaptureExtension.class)
class OdsayFailureLogTest {

	// 실제 키처럼 주소에 넣으면 모양이 바뀌는 글자(+ / =)를 섞은 가짜 키
	private static final String FAKE_KEY = "FAKE+secret/key=123";

	private final OdsayResponseParser parser = new OdsayResponseParser(JsonMapper.builder().build());

	private OdsayClient clientWith(String apiKey, String baseUrl) {
		OdsayProperties properties = new OdsayProperties();
		properties.setApiKey(apiKey);
		properties.setBaseUrl(baseUrl);
		return new OdsayClient(properties);
	}

	@Test
	@DisplayName("인증 실패 - 오디세이가 준 코드 · 문구와 IP 확인 안내가 로그에 남음")
	void authFailed_logsCodeAndHint(CapturedOutput output) {
		assertThatThrownBy(() -> parser.parse(AUTH_FAILED_JSON)).isInstanceOf(TransitApiException.class);

		assertThat(output.getOut())
				.contains("오디세이 호출 실패")
				.contains("code=500")
				.contains("ApiKeyAuthFailed")
				.contains("등록한 서버 IP");
	}

	@Test
	@DisplayName("그 밖의 오류 - 코드 · 문구는 남고, IP 안내는 붙지 않음")
	void otherError_logsWithoutHint(CapturedOutput output) {
		String json = """
				{"error":{"msg":"필수 입력값 형식 및 범위를 확인해주세요.","code":"-8"}}
				""";

		assertThatThrownBy(() -> parser.parse(json)).isInstanceOf(TransitApiException.class);

		assertThat(output.getOut())
				.contains("code=-8")
				.contains("필수 입력값 형식 및 범위를 확인해주세요.")
				.doesNotContain("등록한 서버 IP");
	}

	@Test
	@DisplayName("경로 없음(-98)은 실패가 아니라서 로그를 남기지 않음")
	void noRoute_doesNotLog(CapturedOutput output) {
		assertThat(parser.parse(TOO_CLOSE_JSON)).isEmpty();

		assertThat(output.getOut()).doesNotContain("오디세이 호출 실패");
	}

	@Test
	@DisplayName("JSON이 아닌 응답 - 응답 앞부분이 로그에 남음")
	void invalidJson_logsBodyHead(CapturedOutput output) {
		assertThatThrownBy(() -> parser.parse("<html>Service Unavailable</html>"))
				.isInstanceOf(TransitApiException.class);

		assertThat(output.getOut()).contains("JSON이 아닙니다").contains("Service Unavailable");
	}

	@Test
	@DisplayName("키가 없음 - 설정을 확인하라는 로그가 남음")
	void missingKey_logs(CapturedOutput output) {
		OdsayClient client = clientWith(" ", "http://localhost:1");

		assertThatThrownBy(() -> client.searchPubTransPath(126.97, 37.55, 127.02, 37.49))
				.isInstanceOf(TransitApiException.class);

		assertThat(output.getOut()).contains("키가 설정되지 않았습니다").contains("odsay.api-key");
	}

	@Test
	@DisplayName("연결 실패 - 예외 종류가 로그에 남고, API 키는 어떤 모양으로도 남지 않음")
	void connectionFailed_logsWithoutApiKey(CapturedOutput output) {
		// 아무도 듣고 있지 않은 주소라 바로 연결에 실패한다 (실제 오디세이를 부르지 않는다)
		OdsayClient client = clientWith(FAKE_KEY, "http://localhost:1");

		assertThatThrownBy(() -> client.searchPubTransPath(126.97, 37.55, 127.02, 37.49))
				.isInstanceOf(TransitApiException.class);

		assertThat(output.getAll())
				.contains("오디세이 호출 실패")
				.doesNotContain(FAKE_KEY)
				.doesNotContain("FAKE%2Bsecret%2Fkey%3D123")
				.doesNotContain("secret");
	}
}
