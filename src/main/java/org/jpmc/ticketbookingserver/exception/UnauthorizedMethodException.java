package org.jpmc.ticketbookingserver.exception;

public class UnauthorizedMethodException extends Exception {
    public UnauthorizedMethodException(String message) {
        super(message);
    }
}
