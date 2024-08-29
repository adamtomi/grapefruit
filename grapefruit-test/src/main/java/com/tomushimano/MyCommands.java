package com.tomushimano;

import grapefruit.command.CommandDefinition;
import grapefruit.command.argument.Arg;
import grapefruit.command.argument.Flag;

public class MyCommands {

    @CommandDefinition(route = "print")
    public void printCommand(@Arg String message) {
        System.out.println(message);
    }

    @CommandDefinition(route = "sum")
    public void sumCommand(Object sender, @Arg int a, @Arg int b) {
        int sum = a + b;
        System.out.println("The sum is: " + sum);
    }

    @CommandDefinition(route = "test")
    public void testCommand(
            Object sender,
            @Arg String hello,
            @Flag(name = "test") boolean test,
            @Flag(name = "message", shorthand = 'm') String message
    ) {

    }
}
