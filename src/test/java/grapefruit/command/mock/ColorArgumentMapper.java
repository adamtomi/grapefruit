package grapefruit.command.mock;

import grapefruit.command.argument.mapper.AbstractArgumentMapper;
import grapefruit.command.argument.mapper.ArgumentMappingException;
import grapefruit.command.completion.CompletionAccumulator;
import grapefruit.command.completion.CompletionBuilder;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.MissingInputException;

public class ColorArgumentMapper extends AbstractArgumentMapper<Object, String> {
    private static final char HASH = '#';
    private static final String HEX_ALPHABET = "0123456789abcdef";
    private static final String[] HEX_CHARSET = HEX_ALPHABET.split("");

    public ColorArgumentMapper() {
        super(String.class, false);
    }

    @Override
    public String tryMap(final CommandContext<Object> context, final CommandInputTokenizer input) throws ArgumentMappingException, MissingInputException {
        // We don't really care, whether the provided value is valid
        final String value = input.readWord();
        // Support for CSS-style color shorthands (#xxx)
        if ((value.length() == 7 || value.length() == 4) && value.charAt(0) == HASH && !containsInvalidCharacter(value)) {
            return value;
        }

        throw new ArgumentMappingException();
    }

    private static boolean containsInvalidCharacter(final String input) {
        // Start from index 1 -> skip # symbol
        for (int i = 1; i < input.length(); i++) {
            if (HEX_ALPHABET.indexOf(input.charAt(i)) == -1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public CompletionAccumulator complete(final CommandContext<Object> context, final CompletionBuilder builder) {
        final String input = builder.input();
        if (input.isEmpty()) {
            return builder.includeString(String.valueOf(HASH)).build();
        }

        if (input.length() > 7 || input.charAt(0) != HASH || containsInvalidCharacter(input)) {
            return builder.build();
        } else if (input.length() == 7) {
            return builder.includeString(input).build();
        }

        return builder.includeStrings(HEX_CHARSET, x -> input + x).build();
    }
}
