package kr.ync.triplan.share.exception;

import kr.ync.triplan.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class CommentNotFoundException extends CustomException {

	public CommentNotFoundException() {
		super("해당 댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}
}
