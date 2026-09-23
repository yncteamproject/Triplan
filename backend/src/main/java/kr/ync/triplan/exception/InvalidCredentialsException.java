package kr.ync.triplan.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends CustomException {

	public InvalidCredentialsException() {
		super("이메일 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
	}
}
