package grapefruit.command.runtime.dispatcher;

import grapefruit.command.runtime.CommandAction;
import grapefruit.command.runtime.CommandException;
import grapefruit.command.runtime.argument.CommandArgument;
import grapefruit.command.runtime.dispatcher.condition.CommandCondition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class CommandDefinitionImpl implements CommandDefinition {
    private final @Nullable String permission;
    private final List<CommandArgument<?>> arguments;
    private final List<CommandArgument.Flag<?>> flags;
    private final List<CommandCondition> conditions;
    private final CommandAction action;

    CommandDefinitionImpl(
            @Nullable String permission,
            List<CommandArgument<?>> arguments,
            List<CommandArgument.Flag<?>> flags,
            List<CommandCondition> conditions,
            CommandAction action
    ) {
        this.permission = permission;
        this.arguments = requireNonNull(arguments, "argument cannot be null");
        this.flags = requireNonNull(flags, "flags cannot be null");
        this.conditions = requireNonNull(conditions, "conditions cannot be null");
        this.action = requireNonNull(action, "action cannot be null");
    }

    @Override
    public Optional<String> permission() {
        return Optional.ofNullable(this.permission);
    }

    @Override
    public List<CommandArgument<?>> arguments() {
        return List.copyOf(this.arguments);
    }

    @Override
    public List<CommandArgument.Flag<?>> flags() {
        return List.copyOf(this.flags);
    }

    @Override
    public List<CommandCondition> conditions() {
        return List.copyOf(this.conditions);
    }

    @Override
    public void invoke(CommandContext context) throws CommandException {
        try {
            this.action.invoke(context);
        } catch (CommandException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new CommandInvocationException(ex);
        }
    }
}
