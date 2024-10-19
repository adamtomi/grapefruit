package grapefruit.command.generated;

import grapefruit.command.dispatcher.condition.CommandCondition;
import grapefruit.command.dispatcher.tree.RouteNode;

import java.util.List;

public interface CommandMirror {

    List<RouteNode> route();

    String permission();

    List<Class<? extends CommandCondition>> conditions();
}
