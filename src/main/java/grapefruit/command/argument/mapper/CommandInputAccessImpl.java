package grapefruit.command.argument.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;

import static java.util.Objects.requireNonNull;

final class CommandInputAccessImpl implements CommandInputAccess {
    private final CommandInputTokenizer input;
    private final int cursor;

    CommandInputAccessImpl(final CommandInputTokenizer input) {
        this.input = requireNonNull(input, "input cannot be null");
        this.cursor = input.cursor();
    }

    @Override
    public CommandInputTokenizer input() {
        return this.input;
    }

    @Override
    public String consumedInput() {
        final int end = Math.min(this.input.cursor(), this.input.unwrap().length());
        return this.input.unwrap().substring(this.cursor, end).trim();
    }

    @Override
    public ArgumentMappingException wrapException(final CommandException cause) {
        requireNonNull(cause, "cause cannot be null");
        final int end = Math.min(this.input.cursor(), this.input.unwrap().length());
        return new ArgumentMappingException(
                cause,
                this.input.consumed(),
                this.input.unwrap().substring(this.cursor, end).trim(),
                this.input.remainingOrEmpty()
        );
    }
}
