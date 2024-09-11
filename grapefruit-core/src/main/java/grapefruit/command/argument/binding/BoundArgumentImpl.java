package grapefruit.command.argument.binding;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import static java.util.Objects.requireNonNull;

final class BoundArgumentImpl<T> implements BoundArgument<T> {
    private final CommandArgument<T> argument;
    private final ArgumentMapper<T> mapper;

    BoundArgumentImpl(CommandArgument<T> argument, ArgumentMapper<T> mapper) {
        this.argument = requireNonNull(argument, "argument cannot be null");
        this.mapper = requireNonNull(mapper, "mapper cannot be null");
    }

    @Override
    public CommandArgument<T> argument() {
        return this.argument;
    }

    @Override
    public ArgumentMapper<T> mapper() {
        return this.mapper;
    }

    @Override
    public void consume(CommandContext context, StringReader input) throws CommandException {
        // Use the mapper we're bound to for mapping
        T result = this.mapper.tryMap(context, input);
        // Store the result in the context
        context.put(this.argument.key(), result);
    }

    @Override
    public String toString() {
        return "BoundArgumentImpl(argument=%s, mapper=%s)".formatted(this.argument, this.mapper);
    }
}
