package kr.ync.triplan.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends CustomException {

	public DuplicateEmailException() {
		super("이미 가입된 이메일입니다.", HttpStatus.CONFLICT);
	}
}
