package grapefruit.command.condition;

import grapefruit.command.dispatcher.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public interface CommandCondition<S> {
    Pattern VALID_NAME_PATTERN = Pattern.compile("(\\w|-)+", Pattern.UNICODE_CHARACTER_CLASS);

    @NotNull String id();

    void test(final @NotNull CommandContext<S> context) throws ConditionFailedException;
}
