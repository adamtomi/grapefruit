package grapefruit.command.completion;

import grapefruit.command.argument.CommandArgument;

import java.util.List;
import java.util.Optional;

public interface CompletionContext<S> {

    Optional<String> lastInput();

    Optional<ParsedArgument<S>> lastArgument();

    List<CommandArgument.Required<S, ?>> remainingArguments();

    List<CommandArgument.Flag<S, ?>> remainingFlags();

    boolean currentInputConsumed();

    interface ParsedArgument<S> {

        CommandArgument<S, ?> unwrap(); // TODO Better name

        String consumed();

        boolean needsMoreInput();
    }

    interface Builder<S> {}
}
