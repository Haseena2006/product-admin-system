package com.example.productadmin.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "com.example.productadmin.controller.web")
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    public ModelAndView handleNotFound(ProductNotFoundException ex) {
        return errorPage(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ModelAndView handleDatabase(DataAccessException ex) {
        log.error("Database error", ex);
        return errorPage(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred. Please try again later.");
    }

    private ModelAndView errorPage(HttpStatus status, String message) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("status", status.value());
        mav.addObject("error", status.getReasonPhrase());
        mav.addObject("message", message);
        return mav;
    }
}
