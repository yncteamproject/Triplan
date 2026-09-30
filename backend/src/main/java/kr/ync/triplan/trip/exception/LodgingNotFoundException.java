package kr.ync.triplan.trip.exception;

import kr.ync.triplan.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class LodgingNotFoundException extends CustomException {

	public LodgingNotFoundException() {
		super("해당 숙소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}
}
