package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class DefaultMessageProvider implements MessageProvider {
    private final Map<MessageKey, String> messages = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = -6031910817776585422L;

        {
            put(MessageKeys.AUTHORIZATION_ERROR, "You lack permission '{permission}'");
            put(MessageKeys.FAILED_TO_EXECUTE_COMMAND, "Failed to execute command '{commandline}'");
            put(MessageKeys.NO_SUCH_COMMAND, "Root command node with name '{name}' could not be found");
            put(MessageKeys.TOO_FEW_ARGUMENTS, "Too few arguments have been specified. Syntax: {syntax}");
            put(MessageKeys.TOO_MANY_ARGUMENTS, "Too many arguments have been specified. Syntax: {syntax}");
            put(MessageKeys.UNRECOGNIZED_COMMAND_FLAG, "Command flag {input} is not recognized");
            put(MessageKeys.ILLEGAL_COMMAND_SOURCE, "Illegal command source type ({found}). Required: {required}");

            put(MessageKeys.INVALID_BOOLEAN_VALUE, "'{input}' is not a valid boolean value. Options: {options}");
            put(MessageKeys.INVALID_NUMBER_VALUE, "'{input}' is not a valid number value");
            put(MessageKeys.NUMBER_OUT_OF_RANGE, "{input} is out of range. Value has to be between {min} and {max}");
            put(MessageKeys.QUOTED_STRING_INVALID_TRAILING_CHARATER, "'{input}' has to end with '\"'");
            put(MessageKeys.STRING_REGEX_ERROR, "'{input}' has to match regex {regex}");
            put(MessageKeys.MISSING_FLAG_VALUE, "No flag value specified for flag '{input}'");
            put(MessageKeys.MISSING_FLAG, "You need to specify a flag. Syntax: {syntax}");
        }};
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

    public void register(final @NotNull MessageKey key, final @NotNull String message) {
        requireNonNull(key, "key cannot be null");
        requireNonNull(message, "message cannot be null");
        try {
            this.lock.lock();
            if (this.messages.containsKey(key)) {
                throw new IllegalArgumentException(format("A message is already registered with key %s", key));
            }

            this.messages.put(key, message);
        } finally {
            this.lock.unlock();
        }
    }
}
