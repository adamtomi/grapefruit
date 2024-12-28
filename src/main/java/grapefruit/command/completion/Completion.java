package grapefruit.command.completion;

public interface Completion {

    String content();

    static Completion completion(final String content) {
        return new CompletionImpl(content);
    }

    static Completion empty() {
        return CompletionImpl.EMPTY;
    }
}
