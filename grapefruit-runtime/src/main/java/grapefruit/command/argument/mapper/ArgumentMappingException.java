package grapefruit.command.argument.mapper;

import grapefruit.command.argument.CommandArgumentException;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

/**
 * Thrown by {@link ArgumentMapper mappers} if user input cannot be mapped into
 * some other type.
 */
public class ArgumentMappingException extends CommandArgumentException {
    @Serial
    private static final long serialVersionUID = 7095405742003204193L;
    private final ArgumentMapper<?> mapper;

    public ArgumentMappingException(String input, ArgumentMapper<?> mapper) {
        super(input);
        this.mapper = requireNonNull(mapper, "mapper cannot be null");
    }

    /**
     * @return The mapper that threw this exception
     */
    public ArgumentMapper<?> mapper() {
        return this.mapper;
    }
}
