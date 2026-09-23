package kr.ync.triplan.exception;

import org.springframework.http.HttpStatus;

public class LoginFailedException extends CustomException {

    public LoginFailedException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
    }
}