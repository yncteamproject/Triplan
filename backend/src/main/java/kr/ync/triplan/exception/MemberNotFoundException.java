package kr.ync.triplan.exception;

import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends CustomException {

	public MemberNotFoundException() {
		super("회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}
}
