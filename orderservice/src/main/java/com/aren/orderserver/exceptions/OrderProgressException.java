package com.aren.orderserver.exceptions;

public class OrderProgressException extends RuntimeException {
    public OrderProgressException(String message) {
        super(message);
    }
}
