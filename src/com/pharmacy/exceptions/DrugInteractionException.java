package com.pharmacy.exceptions;

public class DrugInteractionException extends RuntimeException {
    public DrugInteractionException(String message) {
        super(message);
    }
}

