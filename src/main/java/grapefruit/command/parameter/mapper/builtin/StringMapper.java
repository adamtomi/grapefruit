package grapefruit.command.parameter.mapper.builtin;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.Template;
import grapefruit.command.parameter.mapper.AbstractParameterMapper;
import grapefruit.command.parameter.mapper.ParameterMappingException;
import grapefruit.command.parameter.modifier.string.Greedy;
import grapefruit.command.parameter.modifier.string.Quotable;
import grapefruit.command.parameter.modifier.string.Regex;
import grapefruit.command.util.AnnotationList;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringMapper<S> extends AbstractParameterMapper<S, String> {
    private static final char QUOTE_SIGN = '"';

    public StringMapper() {
        super(TypeToken.of(String.class));
    }

    @Override
    public @NotNull String map(final @NotNull CommandContext<S> context,
                               final @NotNull Queue<CommandInput> args,
                               final @NotNull AnnotationList modifiers) throws ParameterMappingException {
        final String parsedValue;
        if (modifiers.has(Greedy.class)) {
            final StringJoiner joiner = new StringJoiner(" ");
            while (!args.isEmpty()) {
                final CommandInput each = args.remove();
                joiner.add(each.rawArg());
                each.markConsumed();
            }

            parsedValue = joiner.toString();
        } else if (modifiers.has(Quotable.class)) {
            final String first = args.element().rawArg();
            if (first.charAt(0) != QUOTE_SIGN) {
                parsedValue = first.trim();

            } else {
                args.remove().markConsumed();
                final StringJoiner joiner = new StringJoiner(" ");
                joiner.add(first.substring(1));
                int joinedCount = 0;
                for (final CommandInput arg : args) {
                    final String rawInput = arg.rawArg();
                    joiner.add(rawInput);
                    joinedCount++;
                    if (Miscellaneous.endsWith(rawInput, QUOTE_SIGN)) {
                        break;
                    }
                }

                // Remove (joinedCount - 1) arguments
                for (int i = 1; i < joinedCount - 1; i++) {
                    final CommandInput arg = args.remove();
                    arg.markConsumed();
                }

                final String joined = joiner.toString();

                /*
                 * If we ran out of arguments in the above loop, this will
                 * make sure to throw an exception, if the last character
                 * isn't QUOTE_SIGN.
                 */
                if (!Miscellaneous.endsWith(joined, QUOTE_SIGN)) {
                    throw new ParameterMappingException(Message.of(MessageKeys.QUOTED_STRING_INVALID_TRAILING_CHARATER));
                }

                parsedValue = joined.substring(0, joined.length() - 1);
            }

        } else {
            parsedValue = args.element().rawArg();
        }

        final Optional<Regex> regexOpt = modifiers.find(Regex.class);
        if (regexOpt.isPresent()) {
            final Regex regex = regexOpt.get();
            final int allowUnicode = regex.allowUnicode() ? Pattern.UNICODE_CHARACTER_CLASS : 0;
            final int caseInsensitive = regex.caseInsensitive() ? Pattern.CASE_INSENSITIVE : 0;
            final int flags = allowUnicode + caseInsensitive;
            final Pattern pattern = Pattern.compile(regex.value(), flags);
            final Matcher matcher = pattern.matcher(parsedValue);

            if (!matcher.matches()) {
                throw new ParameterMappingException(Message.of(
                        MessageKeys.STRING_REGEX_ERROR,
                        Template.of("{input}", parsedValue),
                        Template.of("{regex}", pattern.pattern())
                ));
            }
        }

        return parsedValue;
    }

    @Override
    public boolean suggestionsNeedValidation() {
        return false;
    }
}
