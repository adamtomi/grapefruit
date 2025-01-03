package grapefruit.command.completion;

import java.util.List;

@Deprecated
public interface CompletionAccumulator {

    List<CommandCompletion> filterCompletions();

    static CompletionAccumulator empty() {
        return List::of;
    }
}
