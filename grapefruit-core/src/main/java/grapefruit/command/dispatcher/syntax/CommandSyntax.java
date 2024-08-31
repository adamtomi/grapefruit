package grapefruit.command.dispatcher.syntax;

import grapefruit.command.Command;
import grapefruit.command.argument.CommandArgument;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds basic command syntax information.
 */
public interface CommandSyntax {

    /**
     * The expected prefix of flags in their short form.
     */
    String SHORT_FLAG_PREFIX = "-";
    /**
     * The expected prefix of flags in their long form.
     */
    String LONG_FLAG_PREFIX = "--";

    /**
     * Returns the route of the command that was being executed.
     *
     * @return The route of the command
     */
    String route();

    /**
     * Returns a list of basic argument information.
     *
     * @return List of syntax parts
     */
    List<SyntaxPart> parts();

    /**
     * Creates a new {@link CommandSyntax} instance from
     * the provided {@link Command} instance.
     *
     * @param command The command to generate syntax from
     * @return The generated syntax
     */
    static CommandSyntax create(Command command) {
        List<SyntaxPart> parts = new ArrayList<>();
        // Loop through all command arguments
        for (CommandArgument<?> argument : command.meta().arguments()) {
            // Flags are represented in their long form (as --flagname paramname)
            String format = argument.isFlag()
                    ? "%s%s %s".formatted(LONG_FLAG_PREFIX, argument.name(), argument.name())
                    : argument.name();
            SyntaxPart.Kind kind = argument.isFlag() ? SyntaxPart.Kind.FLAG : SyntaxPart.Kind.ARGUMENT;

            parts.add(new CommandSyntaxImpl.SyntaxPartImpl(format, kind));
        }

        return new CommandSyntaxImpl(command.meta().route(), parts);
    }
}
