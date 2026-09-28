package kr.ync.triplan.exception;

import org.springframework.http.HttpStatus;

public class TravelResultNotFoundException extends CustomException{
    public TravelResultNotFoundException(){
        super("여행 성향 테스트 결과가 없습니다.", HttpStatus.NOT_FOUND);
    }
}
