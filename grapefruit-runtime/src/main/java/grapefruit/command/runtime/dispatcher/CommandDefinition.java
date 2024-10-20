package grapefruit.command.runtime.dispatcher;

import grapefruit.command.runtime.CommandAction;
import grapefruit.command.runtime.argument.CommandArgument;
import grapefruit.command.runtime.dispatcher.condition.CommandCondition;
import grapefruit.command.runtime.dispatcher.tree.RouteNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface CommandDefinition extends CommandAction {

    Optional<String> permission();

    List<CommandArgument<?>> arguments();

    List<CommandArgument.Flag<?>> flags();

    List<CommandCondition> conditions();

    static Builder builder() {
        throw new UnsupportedOperationException();
    }

    interface Builder {

        Builder permission(@Nullable String permission);

        Builder arguments(List<CommandArgument<?>> arguments);

        Builder flags(List<CommandArgument.Flag<?>> flags);

        Builder conditions(List<CommandCondition> conditions);

        Builder action(CommandAction action);

        CommandDefinition build();
    }
}
