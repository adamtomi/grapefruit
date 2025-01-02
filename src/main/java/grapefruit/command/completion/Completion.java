package grapefruit.command.completion;

public interface Completion {

    String content();

    @Deprecated
    static Completion completion(final String content) {
        return new CompletionImpl(content);
    }

    @Deprecated
    static Completion empty() {
        return CompletionImpl.EMPTY;
    }
}
