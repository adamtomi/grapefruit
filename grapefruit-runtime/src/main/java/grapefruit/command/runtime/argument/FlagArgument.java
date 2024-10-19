package grapefruit.command.runtime.argument;

/**
 * A flag argument is a special type of {@link CommandArgument}.
 * Flags have a few key differences compared to regular
 * (aka required) arguments:
 * <ul>
 *     <li>They are optional</li>
 *     <li>They are denoted by --{@link FlagArgument#name()} (long form) or -{@link FlagArgument#shorthand()} (short form)</li>
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
 *         that means their value is set to true, otherwise false. Syntax: <code>--{@link FlagArgument#name()}</code>
 *         or <code>-{@link FlagArgument#shorthand()}</code> for true value, omit for false value.
 *     </li>
 *     <li>
 *         <bold>Value flags</bold> have actual values associated with them. Their value is the obtained by parsing
 *         user input (as with normal {@link CommandArgument arguments}). If they aren't present in the input, the
 *         value is set to null. Syntax: <code>--{@link FlagArgument#name()} 123</code> or <code>-{@link FlagArgument#shorthand}
 *         123</code>
 *     </li>
 * </ul>
 *
 * As with regular arguments, the name and shorthand of a flag must be unique
 * in a command chain.
 *
 * @param <T> The type of values to be supplied by the user to this argument
 */
public interface FlagArgument<T> extends CommandArgument<T> {

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
    boolean isPresenceFlag();
}
