package kr.ync.triplan.global.exception;

import org.springframework.http.HttpStatus;

public class InvalidPageRequestException extends CustomException {

	public InvalidPageRequestException() {
		super("page는 0 이상, size는 1 이상이어야 합니다.", HttpStatus.BAD_REQUEST);
	}
}
