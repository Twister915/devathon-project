package org.devathon.contest2016.command;

public final class CommandException extends Exception {
    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Exception cause) {
        super(message, cause);
    }
}
