package kr.ync.triplan.member.exception;

import kr.ync.triplan.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends CustomException{
    public DuplicateEmailException(){
        super("이미 가입된 이메일입니다.", HttpStatus.CONFLICT);
    }
}
