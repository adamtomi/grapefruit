package grapefruit.command.util;

import grapefruit.command.CommandException;
import grapefruit.command.argument.FlagArgument;
import grapefruit.command.dispatcher.UnrecognizedFlagException;
import grapefruit.command.dispatcher.syntax.CommandSyntax;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * A utility class to make it simpler to detect flags and
 * flag groups in the provided command line.
 */
@SuppressWarnings("rawtypes")
public class FlagGroup implements Iterable<FlagArgument<?>> {
    public static final Pattern VALID_PATTERN = Pattern.compile("^\\%s(\\%s?)([a-zA-Z]\\S*)$".formatted(
            CommandSyntax.SHORT_FLAG_PREFIX,
            CommandSyntax.SHORT_FLAG_PREFIX
    ));
    private final List<FlagArgument<?>> arguments;

    private FlagGroup(List<FlagArgument<?>> arguments) {
        this.arguments = requireNonNull(arguments, "arguments cannot be null");
    }

    public static FlagGroup parse(Matcher matcher, List<FlagArgument> candidates) throws CommandException {
        List<FlagArgument<?>> flags = new ArrayList<>();
        /*
         * If group(1) is empty, that means we're dealing with either
         * 1) a single flag shorthand (such as -a)
         * 2) a flag group consisting of multiple flag shorthands
         */
        if (matcher.group(1).isEmpty()) {
            // Retrieve the group of flag names without the leading slashes
            char[] options = matcher.group(2).toCharArray();
            for (char c : options) {
                flags.add(selectFlag(
                        candidates,
                        x -> x.shorthand() == c,
                        // Throw an exception, because an unrecognized flag shorthand was provided
                        () -> new UnrecognizedFlagException(c)
                ));
            }
        } else {
            // A single long flag name was provided in this case
            String name = matcher.group(2);
            flags.add(selectFlag(
                    candidates,
                    x -> x.name().equalsIgnoreCase(name),
                    // Throw an exception, because an unrecognized flag name was provided
                    () -> new UnrecognizedFlagException(name)
            ));
        }

        return new FlagGroup(flags);
    }

    private static FlagArgument<?> selectFlag(
            List<FlagArgument> candidates,
            Predicate<FlagArgument> test,
            Supplier<CommandException> exceptionFactory
    ) throws CommandException {
        return candidates.stream().filter(test)
                .findFirst()
                .orElseThrow(exceptionFactory);
    }

    public int size() {
        return this.arguments.size();
    }

    @Override
    public @NotNull Iterator<FlagArgument<?>> iterator() {
        return this.arguments.iterator();
    }
}
