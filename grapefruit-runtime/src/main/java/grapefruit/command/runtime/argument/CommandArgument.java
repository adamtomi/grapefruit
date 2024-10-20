package grapefruit.command.runtime.argument;

import grapefruit.command.runtime.argument.mapper.ArgumentMapper;
import grapefruit.command.runtime.argument.modifier.ArgumentModifier;
import grapefruit.command.runtime.dispatcher.CommandContext;
import grapefruit.command.runtime.util.key.Key;
import io.leangen.geantyref.TypeToken;

import java.util.List;

/**
 * Describes an argument of a command. {@link CommandArgument#name()} needs to be
 * unique per argument chain.
 *
 * @param <T> The type of values to be supplied by the user to this argument
 * @see ArgumentMapper
 */
public interface CommandArgument<T> {

    /**
     * The name of the argument. Every argument in
     * an argument list must have a unique name.
     *
     * @return The name of this argument
     */
    String name();

    /**
     * The key of the argument that is used to
     * store and retrieve arguments in and from
     * a {@link CommandContext}.
     *
     * @return The key of this argument
     */
    Key<T> key();

    /**
     * The argument mapper associated with this
     * command argument instance.
     *
     * @return The argument mapper instance
     */
    ArgumentMapper<T> mapper();

    /**
     * Returns whether this argument is a flag argument.
     *
     * @return Whether this argument is a flag argument
     * @see Flag
     */
    boolean isFlag();

    /**
     * Casts this command argument to {@link Flag},
     * if possible.
     *
     * @return This cast to {@link Flag}
     * @see Flag
     */
    Flag<T> asFlag();

    /**
     * Returns an immutable list of argument modifiers associated
     * with this command argument instance.
     *
     * @return List of modifiers
     */
    List<ArgumentModifier<T>> modifiers();

    /**
     * A flag argument is a special type of {@link CommandArgument}.
     * Flags have a few key differences compared to regular
     * (aka required) arguments:
     * <ul>
     *     <li>They are optional</li>
     *     <li>They are denoted by --{@link Flag#name()} (long form) or -{@link Flag#shorthand()} (short form)</li>
     *     <li>
     *         They are not positional, meaning they may appear anywhere in the argument chain (but always after
     *         the command route arguments.
     *     </li>
     *     <li>
     *         Multiple flag arguments may be grouped together in the form "-abc". In this case, the next three
     *         input values are assigned to flag "a", then flag "b" and finally flag "c".
     *     </li>
     * </ul>
     *
     * Flag arguments have two kinds:
     * <ul>
     *     <li>
     *         <bold>Presence flags</bold> are boolean flags, and as the name suggests, if they appear in the user input,
     *         that means their value is set to true, otherwise false. Syntax: <code>--{@link Flag#name()}</code>
     *         or <code>-{@link Flag#shorthand()}</code> for true value, omit for false value.
     *     </li>
     *     <li>
     *         <bold>Value flags</bold> have actual values associated with them. Their value is the obtained by parsing
     *         user input (as with normal {@link CommandArgument arguments}). If they aren't present in the input, the
     *         value is set to null. Syntax: <code>--{@link Flag#name()} 123</code> or <code>-{@link Flag#shorthand}
     *         123</code>
     *     </li>
     * </ul>
     *
     * As with regular arguments, the name and shorthand of a flag must be unique
     * in a command chain.
     *
     * @param <T> The type of values to be supplied by the user to this argument
     */
    interface Flag<T> extends CommandArgument<T> {
        TypeToken<Boolean> PRESENCE_FLAG_TYPE = TypeToken.get(Boolean.class);

        /**
         * The shorthand of the flag, or whitespace, if none was set.
         * Must be unique per argument chain.
         *
         * @return The shorthand associated with this flag.
         */
        char shorthand();

        /**
         * @return Whether this flag is a presence flag.
         */
        boolean isPresence();
    }

    static <T> RequiredBuilder<T> required(String name, Key<T> key) {
        return new CommandArgumentImpl.RequiredBuilder<>(name, key);
    }

    static PresenceFlagBuilder presenceFlag(String name, Key<Boolean> key) {
        return new CommandArgumentImpl.PresenceFlagBuilder(name, key);
    }

    static <T> ValueFlagBuilder<T> valueFlag(String name, Key<T> key) {
        return new CommandArgumentImpl.ValueFlagBuilder<>(name, key);
    }

    interface Builder<T, C extends CommandArgument<T>> {

        C build();
    }

    interface StandardBuilder<T, C extends CommandArgument<T>, B extends Builder<T, C>> extends Builder<T, C> {

        B withMapper(ArgumentMapper<T> mapper);

        B withModifiers(List<ArgumentModifier<T>> modifiers);
    }

    interface FlagBuilder<T, B extends Builder<T, CommandArgument.Flag<T>>> extends Builder<T, CommandArgument.Flag<T>> {

        B shorthand(char shorthand);
    }

    interface RequiredBuilder<T> extends StandardBuilder<T, CommandArgument<T>, RequiredBuilder<T>> {}

    interface PresenceFlagBuilder extends FlagBuilder<Boolean, PresenceFlagBuilder> {}

    interface ValueFlagBuilder<T> extends FlagBuilder<T, ValueFlagBuilder<T>>, StandardBuilder<T, CommandArgument.Flag<T>, ValueFlagBuilder<T>> {}
}
