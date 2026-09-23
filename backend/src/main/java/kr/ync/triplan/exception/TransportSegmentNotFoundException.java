package kr.ync.triplan.exception;

import org.springframework.http.HttpStatus;

public class TransportSegmentNotFoundException extends CustomException {

	public TransportSegmentNotFoundException() {
		super("해당 이동 구간을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}
}
