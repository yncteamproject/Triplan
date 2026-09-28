package kr.ync.triplan.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
