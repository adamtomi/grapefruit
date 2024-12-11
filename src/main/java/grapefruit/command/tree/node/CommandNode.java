package grapefruit.command.tree.node;

import java.util.Set;

public interface CommandNode {

    String name();

    Set<String> aliases();
}
