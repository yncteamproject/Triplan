package kr.ync.triplan.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handlerCustomException(CustomException e) {
		return ResponseEntity
				.status(e.getStatus())
				.body(ErrorResponse.from(e));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		String message
				= e.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
		ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.BAD_REQUEST, message);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(errorResponse);
	}

	// 주소 · 쿼리 값의 형식이 틀림 (예: /api/trips/abc)
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handlerMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
		return response(HttpStatus.BAD_REQUEST, "요청 값의 형식이 올바르지 않습니다.");
	}

	// 본문을 읽을 수 없음 (깨진 JSON, 날짜 칸에 날짜가 아닌 값 등)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handlerHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		return response(HttpStatus.BAD_REQUEST, "요청 본문의 형식이 올바르지 않습니다.");
	}

	// 필수 쿼리 값이 없음 (예: transit-routes에 fromStopId 없음)
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handlerMissingServletRequestParameterException(MissingServletRequestParameterException e) {
		return response(HttpStatus.BAD_REQUEST, "필수 값이 없습니다: " + e.getParameterName());
	}

	// 위에서 처리하지 않은 모든 오류.
	// 여기서 잡지 않으면 스프링이 /error로 넘기는데, 그 주소가 로그인 필요 대상이라 401로 바뀌어 버린다 (K8)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handlerException(Exception e) {
		// 없는 주소(404) · 지원하지 않는 메서드(405)처럼 스프링이 상태 코드를 정해 둔 오류는 그 코드를 그대로 쓴다
		if (e instanceof org.springframework.web.ErrorResponse standard) {
			HttpStatus status = toHttpStatus(standard.getStatusCode());
			if (status.is4xxClientError()) {
				return response(status, clientErrorMessage(status));
			}
		}
		// 자세한 내용은 응답에 넣지 않고 서버 로그에만 남긴다
		log.error("처리하지 않은 오류", e);
		return response(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 오류가 발생했습니다.");
	}

	private HttpStatus toHttpStatus(HttpStatusCode statusCode) {
		HttpStatus status = HttpStatus.resolve(statusCode.value());
		return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
	}

	private String clientErrorMessage(HttpStatus status) {
		return switch (status) {
			case NOT_FOUND -> "요청한 주소를 찾을 수 없습니다.";
			case METHOD_NOT_ALLOWED -> "지원하지 않는 요청 방식입니다.";
			default -> "요청을 처리할 수 없습니다.";
		};
	}

	private ResponseEntity<ErrorResponse> response(HttpStatus status, String message) {
		return ResponseEntity
				.status(status)
				.body(ErrorResponse.of(status, message));
	}
}
