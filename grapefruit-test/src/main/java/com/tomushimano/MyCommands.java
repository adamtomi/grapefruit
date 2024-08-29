package com.tomushimano;

import grapefruit.command.CommandDefinition;
import grapefruit.command.argument.Arg;

public class MyCommands {

    @CommandDefinition(route = "print")
    public void printCommand(@Arg String message) {
        System.out.println(message);
    }
}
