package grapefruit.command.completion;

public interface CommandCompletion {

    String completion();

    static CompletionFactory factory() {
        return CommandCompletionImpl::new;
    }
}
