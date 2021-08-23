package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class MessageKeys {
    private static final Set<MessageKey> values = new HashSet<>();

    public static final MessageKey AUTHORIZATION_ERROR = register(MessageKey.of("dispatcher.authorization-error"));
    public static final MessageKey FAILED_TO_EXECUTE_COMMAND = register(MessageKey.of("dispatcher.failed-to-execute-command"));
    public static final MessageKey NO_SUCH_COMMAND = register(MessageKey.of("dispatcher.no-such-command"));
    public static final MessageKey TOO_FEW_ARGUMENTS = register(MessageKey.of("dispatcher.too-few-arguments"));
    public static final MessageKey TOO_MANY_ARGUMENTS = register(MessageKey.of("dispatcher.too-many-arguments"));
    public static final MessageKey UNRECOGNIZED_COMMAND_FLAG = register(MessageKey.of("dispatcher.unrecognized-command-flag"));

    public static final MessageKey INVALID_BOOLEAN_VALUE = register(MessageKey.of("parameter.invalid-boolean-value"));
    public static final MessageKey INVALID_NUMBER_VALUE = register(MessageKey.of("parameter.invalid-number-value"));
    public static final MessageKey NUMBER_OUT_OF_RANGE = register(MessageKey.of("parameter.number-out-of-range"));
    public static final MessageKey QUOTED_STRING_INVALID_TRAILING_CHARATER = register(MessageKey.of("parameter.quoted-string-invalid-trailing-character"));
    public static final MessageKey STRING_REGEX_ERROR = register(MessageKey.of("parameter.string-regex-error"));

    private static @NotNull MessageKey register(final @NotNull MessageKey key) {
        values.add(key);
        return key;
    }

    public static @NotNull Set<MessageKey> values() {
        return Set.copyOf(values);
    }
}
