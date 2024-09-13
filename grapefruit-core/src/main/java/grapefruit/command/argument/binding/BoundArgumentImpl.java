package grapefruit.command.argument.binding;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import java.util.Objects;

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
        T resultFromMapper = this.mapper.tryMap(context, input);
        // Apply modifiers
        T result = this.argument.modifierChain().applyChain(resultFromMapper);
        // Store the result in the context
        context.put(this.argument.key(), result);
    }

    @Override
    public String toString() {
        return "BoundArgumentImpl(argument=%s, mapper=%s)".formatted(this.argument, this.mapper);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundArgumentImpl<?> that = (BoundArgumentImpl<?>) o;
        return Objects.equals(this.argument, that.argument) && Objects.equals(this.mapper, that.mapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.argument, this.mapper);
    }
}
