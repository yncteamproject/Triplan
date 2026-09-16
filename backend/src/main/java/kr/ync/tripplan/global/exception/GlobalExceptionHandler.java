package kr.ync.tripplan.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 필수값 누락, 형식 위반 등 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("message", "입력값 검증에 실패했습니다.");
        body.put("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Body 자체가 없거나 JSON 형식이 깨진 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("message", "요청 본문이 없거나 형식이 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 중복 이메일 등 비즈니스 로직 예외
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}