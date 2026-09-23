package kr.ync.triplan.exception;

import org.springframework.http.HttpStatus;

public class LodgingNotFoundException extends CustomException {

	public LodgingNotFoundException() {
		super("해당 숙소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}
}
