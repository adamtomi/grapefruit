package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class DefaultMessageProvider implements MessageProvider {
    private final Map<MessageKey, String> messages = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(MessageKeys.AUTHORIZATION_ERROR, "You lack permission '{permission}'"),
            new AbstractMap.SimpleEntry<>(MessageKeys.FAILED_TO_EXECUTE_COMMAND, "Failed to execute command '{commandline}'"),
            new AbstractMap.SimpleEntry<>(MessageKeys.NO_SUCH_COMMAND, "Root command node with name '{name}' could not be found"),
            new AbstractMap.SimpleEntry<>(MessageKeys.TOO_FEW_ARGUMENTS, "Too few arguments have been specified"),
            new AbstractMap.SimpleEntry<>(MessageKeys.TOO_MANY_ARGUMENTS, "Too many arguments have been specified"),
            new AbstractMap.SimpleEntry<>(MessageKeys.UNRECOGNIZED_COMMAND_FLAG, "Command flag {value} is not recognized"),

            new AbstractMap.SimpleEntry<>(MessageKeys.INVALID_BOOLEAN_VALUE, "'{input}' is not a valid boolean value. Options: {options}"),
            new AbstractMap.SimpleEntry<>(MessageKeys.INVALID_NUMBER_VALUE, "'{input}' is not a valid number value"),
            new AbstractMap.SimpleEntry<>(MessageKeys.NUMBER_OUT_OF_RANGE, "{input} is out of range. Value has to be between {min} and {max}"),
            new AbstractMap.SimpleEntry<>(MessageKeys.QUOTED_STRING_INVALID_TRAILING_CHARATER, "'{input}' has to end with '\"'"),
            new AbstractMap.SimpleEntry<>(MessageKeys.STRING_REGEX_ERROR, "'{input}' has to match regex {regex}")
    );
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public @NotNull String provide(final @NotNull MessageKey key) {
        requireNonNull(key, "key cannot be null");
        try {
            this.lock.lock();
            final @Nullable String result = this.messages.get(key);
            if (result == null) {
                throw new IllegalArgumentException(format("Unrecognized MessageKey: %s", key));
            }

            return result;
        } finally {
            this.lock.unlock();
        }
    }
}
