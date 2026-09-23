package kr.ync.triplan.exception;

import org.springframework.http.HttpStatus;

public class StopNotFoundException extends CustomException {

	public StopNotFoundException() {
		super("해당 방문지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}
}
