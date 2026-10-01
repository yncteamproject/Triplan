package kr.ync.triplan.global.exception;

import kr.ync.triplan.member.controller.MemberController;
import kr.ync.triplan.traveltest.controller.TravelPreferenceController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import kr.ync.triplan.member.controller.AuthController;

// GlobalExceptionHandler(팀 공통)에 없는 예외만 처리 → 서로 겹치지 않게
// 마이페이지/성향 테스트 컨트롤러에만 적용 (팀원 컨트롤러 동작은 건드리지 않음)
@RestControllerAdvice(assignableTypes = {AuthController.class, MemberController.class, TravelPreferenceController.class})
public class CommonExceptionHandler {

    // Body 자체가 없거나 JSON 형식이 깨진 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "요청 본문이 없거나 형식이 올바르지 않습니다."));
    }

    // 잘못된 입력값 (예: TravelPreferenceService "답변이 없습니다.")
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage()));
    }
}