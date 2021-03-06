package org.example.matchmakingdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice(annotations = {RestController.class})
@Slf4j
public class ControllerAdvise {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleException(Exception ex) {
		log.error(ex.getMessage(), ex);
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		return new ResponseEntity<>(ex.getMessage(), status);
	}

}
