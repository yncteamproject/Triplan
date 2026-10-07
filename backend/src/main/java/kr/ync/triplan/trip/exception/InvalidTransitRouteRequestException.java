package kr.ync.triplan.trip.exception;

import kr.ync.triplan.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidTransitRouteRequestException extends CustomException {

	public InvalidTransitRouteRequestException(String message) {
		super(message, HttpStatus.BAD_REQUEST);
	}
}
