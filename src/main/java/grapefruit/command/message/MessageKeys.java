package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class MessageKeys {
    private static final Set<MessageKey> values = new HashSet<>();

    // Variables: {id}
    public static final MessageKey CONDITION_FAILED = register(MessageKey.of("condition.failed"));

    // Variables: {permission}
    public static final MessageKey AUTHORIZATION_ERROR = register(MessageKey.of("dispatcher.authorization-error"));
    // Variables: {commandline}
    public static final MessageKey FAILED_TO_EXECUTE_COMMAND = register(MessageKey.of("dispatcher.failed-to-execute-command"));
    // Variables: {name}
    public static final MessageKey NO_SUCH_COMMAND = register(MessageKey.of("dispatcher.no-such-command"));
    // Variables: {syntax}
    public static final MessageKey TOO_FEW_ARGUMENTS = register(MessageKey.of("dispatcher.too-few-arguments"));
    // Variables: {syntax}
    public static final MessageKey TOO_MANY_ARGUMENTS = register(MessageKey.of("dispatcher.too-many-arguments"));
    // Variables: {found}, {required}
    public static final MessageKey ILLEGAL_COMMAND_SOURCE = register(MessageKey.of("dispatcher.illegal-command-source"));

    // Variables: {input} {options}
    public static final MessageKey INVALID_BOOLEAN_VALUE = register(MessageKey.of("parameter.invalid-boolean-value"));
    // Variables: {input}
    public static final MessageKey INVALID_NUMBER_VALUE = register(MessageKey.of("parameter.invalid-number-value"));
    // Variables: {input}
    public static final MessageKey INVALID_CHARACTER_VALUE = register(MessageKey.of("parameter.invalid-character-value"));
    // Variables: {input}, {min}, {max}
    public static final MessageKey NUMBER_OUT_OF_RANGE = register(MessageKey.of("parameter.number-out-of-range"));
    // Variables: {input}
    public static final MessageKey QUOTED_STRING_INVALID_TRAILING_CHARATER =
            register(MessageKey.of("parameter.quoted-string-invalid-trailing-character"));
    // Variables: {input} {regex}
    public static final MessageKey STRING_REGEX_ERROR = register(MessageKey.of("parameter.string-regex-error"));
    // Variables: {input}
    public static final MessageKey MISSING_FLAG_VALUE = register(MessageKey.of("parameter.missing-flag-value"));
    // Variables: {syntax}
    public static final MessageKey MISSING_FLAG = register(MessageKey.of("parameter.missing-flag"));
    // Variables: {flag}
    public static final MessageKey DUPLICATE_FLAG = register(MessageKey.of("parameter.duplicate.flag"));
    // Variables: {input}
    public static final MessageKey UNRECOGNIZED_COMMAND_FLAG = register(MessageKey.of("parameter.unrecognized-command-flag"));

    private static @NotNull MessageKey register(final @NotNull MessageKey key) {
        values.add(key);
        return key;
    }

    public static @NotNull Set<MessageKey> values() {
        return Set.copyOf(values);
    }
}
