package grapefruit.command.dispatcher.input;

import grapefruit.command.CommandException;

import java.io.Serial;

public class MissingInputException extends CommandException {
    @Serial
    private static final long serialVersionUID = 400460995552207098L;

    public MissingInputException() {
        super();
    }
}
