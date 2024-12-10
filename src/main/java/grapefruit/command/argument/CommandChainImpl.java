package grapefruit.command.argument;

import java.util.ArrayList;
import java.util.List;

import static grapefruit.command.util.StringUtil.containsIgnoreCase;
import static grapefruit.command.util.StringUtil.containsWhitespace;
import static java.util.Objects.requireNonNull;

final class CommandChainImpl<S> implements CommandChain<S> {
    private final List<CommandArgument.Literal<S>> route;
    private final List<CommandArgument.Required<S, ?>> arguments;
    private final List<CommandArgument.Flag<S, ?>> flags;

    CommandChainImpl(
            final List<CommandArgument.Literal<S>> route,
            final List<CommandArgument.Required<S, ?>> arguments,
            final List<CommandArgument.Flag<S, ?>> flags
    ) {
        this.route = requireNonNull(route, "route cannot be null");
        this.arguments = requireNonNull(arguments, "arguments cannot be null");
        this.flags = requireNonNull(flags, "flags cannot be null");
    }

    @Override
    public List<CommandArgument.Literal<S>> route() {
        return List.copyOf(this.route);
    }

    @Override
    public List<CommandArgument.Required<S, ?>> arguments() {
        return List.copyOf(this.arguments);
    }

    @Override
    public List<CommandArgument.Flag<S, ?>> flags() {
        return List.copyOf(this.flags);
    }

    static <S> CommandChain.LiteralBuilder<S> begin() {
        return new LiteralBuilder<>();
    }

    private static abstract class BaseBuilder<S, T, B extends Builder<S, T, B>> implements Builder<S, T, B> {
        protected final List<T> elements = new ArrayList<>();

        protected abstract void validate(final T element);

        protected abstract B self();

        @Override
        public B then(final T element) {
            requireNonNull(element, "element cannot be null");
            validate(element);
            this.elements.add(element);
            return self();
        }
    }

    private static final class LiteralBuilder<S> extends BaseBuilder<S, CommandArgument.Literal<S>, CommandChain.LiteralBuilder<S>> implements CommandChain.LiteralBuilder<S> {

        @Override
        protected void validate(final CommandArgument.Literal<S> element) {
            final String name = element.name();
            validate(name, "name");

            for (final String alias : element.aliases()) {
                validate(alias, "alias");
            }
        }

        private void validate(final String value, final String label) {
            if (value.isBlank()) {
                throw new IllegalArgumentException("Literal argument %s '%s' is blank".formatted(label, value));
            }

            // Check if the argument contains whitespace surrounded by non-whitespace characters
            if (containsWhitespace(value)) {
                throw new IllegalArgumentException("Literal argument %s '%s' contains whitespace".formatted(label, value));
            }
        }

        @Override
        protected CommandChain.LiteralBuilder<S> self() {
            return this;
        }

        @Override
        public CommandChain.ArgumentBuilder<S> arguments() {
            return new ArgumentBuilder<>(this.elements);
        }

        @Override
        public CommandChain.FlagBuilder<S> flags() {
            return new FlagBuilder<>(this.elements, List.of());
        }

        @Override
        public CommandChain<S> build() {
            if (this.elements.isEmpty()) {
                throw new IllegalStateException("No command route part has been added yet");
            }

            return new CommandChainImpl<>(this.elements, List.of(), List.of());
        }
    }

    private static final class ArgumentBuilder<S> extends BaseBuilder<S, CommandArgument.Required<S, ?>, CommandChain.ArgumentBuilder<S>> implements CommandChain.ArgumentBuilder<S> {
        private final List<CommandArgument.Literal<S>> route;

        private ArgumentBuilder(List<CommandArgument.Literal<S>> route) {
            this.route = requireNonNull(route, "route cannot be null");
        }

        @Override
        protected void validate(CommandArgument.Required<S, ?> element) {
            if (containsIgnoreCase(element.name(), this.elements, CommandArgument::name)) {
                throw new IllegalStateException("A command argument with name \"%s\" is already in the list".formatted(element.name()));
            }

            if (!this.elements.isEmpty() && this.elements.getLast().mapper().isTerminal()) {
                throw new IllegalStateException("Cannot register command argument after a terminal argument");
            }
        }

        @Override
        protected ArgumentBuilder<S> self() {
            return this;
        }

        @Override
        public FlagBuilder<S> flags() {
            if (!this.elements.isEmpty() && this.elements.getLast().mapper().isTerminal()) {
                throw new IllegalStateException("Cannot register command flag after terminal argument");
            }

            return new FlagBuilder<>(this.route, this.elements);
        }

        @Override
        public CommandChain<S> build() {
            if (this.route.isEmpty()) {
                throw new IllegalStateException("No command route part has been added yet");
            }

            return new CommandChainImpl<>(this.route, this.elements, List.of());
        }
    }

    private static final class FlagBuilder<S> extends BaseBuilder<S, CommandArgument.Flag<S, ?>, CommandChain.FlagBuilder<S>> implements CommandChain.FlagBuilder<S> {
        private final List<CommandArgument.Literal<S>> route;
        private final List<CommandArgument.Required<S, ?>> arguments;

        private FlagBuilder(final List<CommandArgument.Literal<S>> route, final List<CommandArgument.Required<S, ?>> arguments) {
            this.route = requireNonNull(route, "route cannot be null");
            this.arguments = requireNonNull(arguments, "arguments cannot be null");
        }

        @Override
        protected void validate(final CommandArgument.Flag<S, ?> element) {
            if (element.mapper().isTerminal()) {
                throw new IllegalStateException("Command flags cannot have a terminal argument mapper");
            }

            if (containsIgnoreCase(element.name(), this.arguments, CommandArgument::name)) {
                throw new IllegalStateException("A command argument with name \"%s\" is already in the list".formatted(element.name()));
            }

            if (containsIgnoreCase(element.name(), this.elements, CommandArgument::name)) {
                throw new IllegalStateException("A flag with name \"%s\" is already in the list".formatted(element.name()));
            }

            final char shorthand = element.shorthand();
            if (shorthand == 0) return;

            if (!Character.isAlphabetic(shorthand)) {
                throw new IllegalArgumentException("Flag shorthand must be alphabetic, \"%s\" is not".formatted(shorthand));
            }

            if (this.elements.stream().anyMatch(x -> x.shorthand() == shorthand)) {
                throw new IllegalStateException("A flag with shorthand \"%s\" is already in the list".formatted(element.shorthand()));
            }
        }

        @Override
        protected CommandChain.FlagBuilder<S> self() {
            return this;
        }

        @Override
        public CommandChain<S> build() {
            if (this.route.isEmpty()) {
                throw new IllegalStateException("No command route part has been added yet");
            }

            return new CommandChainImpl<>(this.route, this.arguments, this.elements);
        }
    }
}
