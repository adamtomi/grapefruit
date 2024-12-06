package grapefruit.command.util;

import java.io.Serial;

public final class DontInvokeMe extends UnsupportedOperationException {
    @Serial
    private static final long serialVersionUID = 7597157519196058847L;

    public DontInvokeMe() {
        super("This constructor is private as it's not supposed to be invoked.");
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
