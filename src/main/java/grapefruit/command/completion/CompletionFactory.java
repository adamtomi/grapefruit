package grapefruit.command.completion;

@FunctionalInterface
public interface CompletionFactory {

    CommandCompletion create(final String completion);
}
