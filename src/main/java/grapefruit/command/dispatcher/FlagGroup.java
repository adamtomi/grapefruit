package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.exception.UnrecognizedFlagException;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class FlagGroup<S> implements Iterable<FlagParameter<S>> {
    protected static final Pattern VALID_PATTERN = Pattern.compile("^-(-?)([a-zA-Z]\\S*)$");
    private final Set<FlagParameter<S>> flags;

    private FlagGroup(final @NotNull Set<FlagParameter<S>> flags) {
        this.flags = requireNonNull(flags);
    }

    public static <S> @NotNull FlagGroup<S> parse(final @NotNull String rawInput,
                                                  final @NotNull Matcher matcher,
                                                  final @NotNull List<CommandParameter<S>> parameters) throws CommandException {
        final Set<FlagParameter<S>> flags = new LinkedHashSet<>();
        // This means that flags are grouped together (like -abc)
        if (matcher.group(1).isEmpty()) {
            final String group = matcher.group(2);
            for (final char shorthand : group.toCharArray()) {
                final FlagParameter<S> flag = parameters.stream()
                        .filter(CommandParameter::isFlag)
                        .map(x -> (FlagParameter<S>) x)
                        .filter(x -> x.shorthand() == shorthand)
                        .findFirst()
                        .orElseThrow(() -> new UnrecognizedFlagException(String.valueOf(shorthand)));
                flags.add(flag);
            }

        } else {
            final String name = matcher.group(2);
            final FlagParameter<S> flag = parameters.stream()
                    .filter(CommandParameter::isFlag)
                    .map(x -> (FlagParameter<S>) x)
                    .filter(x -> x.flagName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new UnrecognizedFlagException(rawInput));
            flags.add(flag);
        }

        return new FlagGroup<>(flags);
    }

    public int count() {
        return this.flags.size();
    }

    @Override
    public @NotNull Iterator<FlagParameter<S>> iterator() {
        return this.flags.iterator();
    }
}
