package com.example.productadmin.exception;

public class DuplicateProductException extends RuntimeException {

    public DuplicateProductException(String name) {
        super("A product named '" + name + "' already exists");
    }
}
