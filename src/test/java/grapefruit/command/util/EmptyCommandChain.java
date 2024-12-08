package grapefruit.command.util;

import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.CommandChain;

import java.util.List;

public class EmptyCommandChain implements CommandChain<Object> {

    @Override
    public List<CommandArgument.Literal<Object>> route() {
        return List.of();
    }

    @Override
    public List<CommandArgument.Required<Object, ?>> arguments() {
        return List.of();
    }

    @Override
    public List<CommandArgument.Flag<Object, ?>> flags() {
        return List.of();
    }
}
