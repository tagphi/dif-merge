package com.rtbasia.difmerge.aop;

import com.rtbasia.difmerge.http.SubmitResponse;
import com.rtbasia.difmerge.validator.FileFormatException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FileFormatException.class)
    @ResponseBody
    protected ResponseEntity<Object> fileFormatErrorHandler(HttpServletRequest req, Exception e) {
        return new ResponseEntity<>(SubmitResponse.error(e.getMessage()), HttpStatus.OK);
    }
}
