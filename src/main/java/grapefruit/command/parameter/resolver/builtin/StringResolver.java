package grapefruit.command.parameter.resolver.builtin;

import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.modifier.string.Greedy;
import grapefruit.command.parameter.modifier.string.Quoted;
import grapefruit.command.parameter.modifier.string.Regex;
import grapefruit.command.parameter.resolver.AbstractParamterResolver;
import grapefruit.command.parameter.resolver.ParameterResolutionException;
import grapefruit.command.util.Miscellaneous;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class StringResolver<S> extends AbstractParamterResolver<S, String> {
    private static final char QUOTE_SIGN = '"';

    public StringResolver() {
        super(TypeToken.get(String.class));
    }

    @Override
    public @NotNull String resolve(final @NotNull S source,
                                   final @NotNull Queue<String> args,
                                   final @NotNull CommandParameter param) throws ParameterResolutionException {
        System.out.println("resolve");
        if (args.isEmpty()) {
            throw new NoSuchElementException();
        }

        final String parsedValue;
        if (param.modifiers().has(Greedy.class)) {
            System.out.println("greedy");
            final StringJoiner joiner = new StringJoiner(" ");
            while (!args.isEmpty()) {
                joiner.add(args.remove());
            }

            parsedValue = joiner.toString();
        } else if (param.modifiers().has(Quoted.class)) {
            System.out.println("quoted");
            final String first = args.remove().trim();
            System.out.println(first);
            if (first.charAt(0) != QUOTE_SIGN) {
                System.out.println("isn't quoted, result is first ^");
                parsedValue = first;

            } else {
                System.out.println("quoted");
                final StringJoiner joiner = new StringJoiner(" ");
                joiner.add(first.substring(1));
                String each;
                while ((each = args.poll()) != null) {
                    joiner.add(each);
                    if (Miscellaneous.endsWith(each, QUOTE_SIGN)) {
                        break;
                    }
                }

                final String joined = joiner.toString();
                System.out.println(joined);

                /*
                 * If we ran out of arguments in the above loop, this will
                 * make sure to throw an exception, if the last character
                 * isn't QUOTE_SIGN.
                 */
                if (!Miscellaneous.endsWith(joined, QUOTE_SIGN)) {
                    throw new ParameterResolutionException("Parameter must end with \"");
                }

                parsedValue = joined.substring(0, joined.length() - 1);
                System.out.println(parsedValue);
            }

        } else {
            parsedValue = args.element();
        }

        final Optional<Regex> regexOpt = param.modifiers().find(Regex.class);
        if (regexOpt.isPresent()) {
            final Regex regex = regexOpt.get();
            final int allowUnicode = regex.allowUnicode() ? Pattern.UNICODE_CHARACTER_CLASS : 0;
            final int caseInsensitive = regex.caseInsensitive() ? Pattern.CASE_INSENSITIVE : 0;
            final Pattern pattern = Pattern.compile(regex.value(), allowUnicode & caseInsensitive);
            final Matcher matcher = pattern.matcher(parsedValue);
            if (!matcher.matches()) {
                throw new ParameterResolutionException(format("Parameter must match regex %s", pattern.pattern()));
            }
        }

        return parsedValue;
    }

    @Override
    public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                 final @NotNull String currentArg,
                                                 final @NotNull CommandParameter param) {
        return List.of(currentArg);
    }
}
