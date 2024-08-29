package com.tomushimano;

import grapefruit.command.CommandDefinition;
import grapefruit.command.argument.Arg;

public class TestCommands {

    @CommandDefinition(route = "tell")
    public void tellCommand(Object sender, @Arg Object target, @Arg String message) {
        System.out.println("Sender tells target: " + message);
    }
}
