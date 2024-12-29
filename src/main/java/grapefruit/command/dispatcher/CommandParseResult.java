package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.CommandChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface CommandParseResult<S> {

    Optional<CommandException> capturedException();

    void throwException() throws CommandException;

    /*
     * The last argument that was being parsed.
     */
    Optional<CommandArgument.Dynamic<S, ?>> lastArgument();

    /*
     * A list of arguments that were not consumed yet.
     */
    List<CommandArgument.Required<S, ?>> remainingArguments();

    /*
     * A list of flags that were not consumed yet.
     */
    List<CommandArgument.Flag<S, ?>> remainingFlags();

    boolean isComplete();

    static <S> Builder<S> createBuilder(final CommandChain<S> chain) {
        // Make mutable copies
        return new CommandParseResultImpl.Builder<>(new ArrayList<>(chain.arguments()), new ArrayList<>(chain.flags()));
    }

    interface Builder<S> {

        void begin(final CommandArgument.Dynamic<S, ?> argument);

        void end();

        void capture(final CommandException ex);

        CommandParseResult<S> build();
    }
}
