package kr.ync.triplan.trip.exception;

import kr.ync.triplan.global.exception.CustomException;
import org.springframework.http.HttpStatus;

// 오디세이 호출 실패 (키 없음 · 키 오류 · 서버 장애 · 시간 초과 · 하루 호출 횟수 초과)
public class TransitApiException extends CustomException {

	public TransitApiException() {
		super("대중교통 정보를 불러오지 못했습니다.", HttpStatus.BAD_GATEWAY);
	}
}
