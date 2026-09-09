package kr.ync.triplan.exception;

import org.springframework.http.HttpStatus;

public class TripNotFoundException extends CustomException {

	public TripNotFoundException() {
		super("여행 계획을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}
}
