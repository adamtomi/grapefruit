package grapefruit.command.argument;

import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.util.key.Keyed;

import java.util.Optional;
import java.util.Set;

public interface CommandArgument<T> extends Keyed<T> {

    String name();

    Optional<String> permission();

    interface Builder<T, C extends CommandArgument<T>, B extends Builder<T, C, B>> {

        B require(final String permission);

        C build();
    }

    interface Literal extends CommandArgument<String> {

        Set<String> aliases();

        interface Builder extends CommandArgument.Builder<String, Literal, Builder> {

            Builder aliases(final String... aliases);
        }
    }

    interface Dynamic<S, T> extends CommandArgument<T> {

        ArgumentMapper<S, T> mapper();
    }

    interface Required<S, T> extends Dynamic<S, T> {

        interface Builder<S, T> extends CommandArgument.Builder<T, Required<S, T>, Builder<S, T>> {

            Builder<S, T> mapWith(final ArgumentMapper<S, T> mapper);
        }
    }

    interface Flag<S, T> extends Dynamic<S, T> {

        char shorthand();

        ArgumentMapper<S, T> mapper();

        interface Builder<S, T, B extends Builder<S, T, B>> extends CommandArgument.Builder<T, Flag<S, T>, Builder<S, T, B>> {

            B shorthand(final char shorthand);

            B assumeShorthand();
        }

        interface PresenceBuilder<S> extends Builder<S, Boolean, PresenceBuilder<S>> {}

        interface ValueBuilder<S, T> extends Builder<S, T, ValueBuilder<S, T>> {

            ValueBuilder<S, T> mapWith(final ArgumentMapper<S, T> mapper);
        }
    }
}
