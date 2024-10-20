package grapefruit.command.runtime.dispatcher.syntax;

import grapefruit.command.runtime.dispatcher.tree.RouteNode;

import java.util.List;

record CommandSyntaxImpl(List<RouteNode> route, List<SyntaxPart> parts) implements CommandSyntax {

    record SyntaxPartImpl(String format, SyntaxPart.Kind kind) implements SyntaxPart {}
}
