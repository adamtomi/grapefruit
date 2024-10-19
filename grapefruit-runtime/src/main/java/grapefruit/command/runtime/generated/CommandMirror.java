package grapefruit.command.runtime.generated;

import grapefruit.command.runtime.dispatcher.condition.CommandCondition;
import grapefruit.command.runtime.dispatcher.tree.RouteNode;

import java.util.List;

public interface CommandMirror {

    List<RouteNode> route();

    String permission();

    List<Class<? extends CommandCondition>> conditions();
}
