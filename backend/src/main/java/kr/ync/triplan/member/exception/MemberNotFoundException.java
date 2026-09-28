package kr.ync.triplan.member.exception;

import kr.ync.triplan.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends CustomException {

    public MemberNotFoundException() {
        super("존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND);
    }
}
