package grapefruit.command.dispatcher;

import grapefruit.command.parameter.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

class SuggestionContext<S> {
    private @Nullable CommandParameter<S> parameter;
    private @Nullable CommandInput input;
    private boolean flagNameConsumed;
    private boolean suggestNext;

    public @NotNull Optional<CommandParameter<S>> parameter() {
        return Optional.ofNullable(this.parameter);
    }

    public void parameter(final @Nullable CommandParameter<S> parameter) {
        this.parameter = parameter;
    }

    public @NotNull Optional<CommandInput> input() {
        return Optional.ofNullable(this.input);
    }

    public void input(final @Nullable CommandInput input) {
        this.input = input;
    }

    public boolean flagNameConsumed() {
        return this.flagNameConsumed;
    }

    public void flagNameConsumed(final boolean flagNameConsumed) {
        this.flagNameConsumed = flagNameConsumed;
    }

    public boolean suggestNext() {
        return this.suggestNext;
    }

    public void suggestNext(boolean suggestNext) {
        this.suggestNext = suggestNext;
    }

    @Override
    public String toString() {
        return "SuggestionContext[" +
                "parameter=" + this.parameter +
                ", input=" + this.input +
                ", flagNameConsumed=" + this.flagNameConsumed +
                ", suggestNext=" + this.suggestNext +
                ']';
    }
}
