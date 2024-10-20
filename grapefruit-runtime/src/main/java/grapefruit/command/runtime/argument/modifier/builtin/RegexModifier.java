package grapefruit.command.runtime.argument.modifier.builtin;

import grapefruit.command.runtime.argument.CommandArgumentException;
import grapefruit.command.runtime.argument.modifier.ArgumentModifier;
import grapefruit.command.runtime.util.key.Key;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexModifier implements ArgumentModifier<String> {
    private static final Key<String> REGEX_KEY = Key.named(String.class, "value");
    private static final Key<Integer> FLAGS_KEY = Key.named(Integer.class, "flags");
    private final Pattern pattern;

    private RegexModifier(Context context) {
        String value = context.require(REGEX_KEY);
        int flags = context.require(FLAGS_KEY);
        this.pattern = Pattern.compile(value, flags);
    }

    @Override
    public String apply(String input) throws CommandArgumentException {
        Matcher matcher = this.pattern.matcher(input);
        if (!matcher.matches()) throw generateException(input);

        return input;
    }

    public static final class Factory implements ArgumentModifier.Factory<String> {

        @Override
        public ArgumentModifier<String> createFromContext(Context context) {
            return new RegexModifier(context);
        }
    }
}
