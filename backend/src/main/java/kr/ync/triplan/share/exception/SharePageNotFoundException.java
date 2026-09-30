package kr.ync.triplan.share.exception;

import kr.ync.triplan.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SharePageNotFoundException extends CustomException {

	public SharePageNotFoundException() {
		super("해당 게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}
}