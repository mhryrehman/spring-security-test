package com.example.spring_security_test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<String> handleRunTimeException(){
        return new ResponseEntity<>("Run time exception occured " , HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException(){
        return new ResponseEntity<>("NullPointer exception occured " , HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> handleException(){
        return new ResponseEntity<>("exception occured " , HttpStatus.BAD_REQUEST);
    }
}
