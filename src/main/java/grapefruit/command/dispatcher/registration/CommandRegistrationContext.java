package grapefruit.command.dispatcher.registration;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@Deprecated
public record CommandRegistrationContext<S> (@NotNull List<String> route, @NotNull CommandRegistration<S> registration) {}
