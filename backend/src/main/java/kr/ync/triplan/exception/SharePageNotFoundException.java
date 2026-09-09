package kr.ync.triplan.exception;

import org.springframework.http.HttpStatus;

public class SharePageNotFoundException extends CustomException {

	public SharePageNotFoundException() {
		super("해당 게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}
}