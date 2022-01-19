package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.registration.CommandRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record CommandSyntax (@NotNull String rawSyntax,
                             @NotNull List<String> syntaxOptions,
                             @NotNull Optional<CommandRegistration<?>> registration) {}
