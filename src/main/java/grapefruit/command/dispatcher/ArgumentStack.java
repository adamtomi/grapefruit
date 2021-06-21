package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

public class ArgumentStack {
    private final String[] args;
    private int index = 0;

    public ArgumentStack(final @NotNull String[] args) {
        this.args = requireNonNull(args, "args cannot be null");
    }

    public @NotNull String arg() {
        if (this.index >= this.args.length) {
            throw new NoSuchElementException();
        }

        return this.args[this.index];
    }

    public @NotNull String next() {
        
    }
}
