package grapefruit.command.mock;

import grapefruit.command.argument.mapper.AbstractArgumentMapper;
import grapefruit.command.argument.mapper.ArgumentMappingException;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.MissingInputException;

import java.util.Arrays;
import java.util.List;

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
        if (value.length() == 7 && value.charAt(0) == HASH && !containsInvalidCharacter(value)) {
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
    public List<String> complete(final CommandContext<Object> context, final String input) {
        if (input.isEmpty()) {
            return List.of(String.valueOf(HASH));
        }

        if (input.length() > 7 || input.charAt(0) != HASH || containsInvalidCharacter(input)) {
            return List.of();
        }

        return Arrays.stream(HEX_CHARSET)
                .map(x -> input + x)
                .toList();
    }
}
