package grapefruit.command.argument.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Argument mappers are responsible for parsing data from its
 * {@link String} representation into other data types.
 *
 * @param <T> The other data type
 */
public interface ArgumentMapper<T> {

    /**
     * Maps user input from its {@link String} representation
     * into some other data type.
     *
     * @param context The current command context
     * @param input The reader reading user input
     * @return The mapped data
     * @throws CommandException If the reader fails
     * or the provided data is invalid.
     */
    T tryMap(CommandContext context, StringReader input) throws CommandException;

    /**
     * Lists completions based on current user input.
     *
     * @param context The current command context
     * @param input The input to suggest for
     * @return List of completions
     */
    List<String> complete(CommandContext context, String input);

    /**
     * Returns a new {@link ArgumentMappingException} instance assoicated with
     * this mapper instance.
     *
     * @param input The user input that was parsed
     * @return The generated exception
     */
    default ArgumentMappingException generateException(String input) {
        return new ArgumentMappingException(input, this);
    }

    /**
     * Constructs a new {@link ArgumentMapper} instance
     * that maps any input into the supplied value.
     *
     * @param <T> The value type
     * @param value The value to return
     * @return The constructed mapper
     */
    static <T> ArgumentMapper<T> constant(T value) {
        return new Constant<>(value);
    }

    final class Constant<T> implements ArgumentMapper<T> {
        private final T value;

        private Constant(T value) {
            this.value = requireNonNull(value, "value cannot be null");
        }

        @Override
        public T tryMap(CommandContext context, StringReader input) {
            System.out.println("Constant tryMap");
            System.out.println(this.value);
            return this.value;
        }

        @Override
        public List<String> complete(CommandContext context, String input) {
            return List.of(String.valueOf(this.value));
        }
    }
}
