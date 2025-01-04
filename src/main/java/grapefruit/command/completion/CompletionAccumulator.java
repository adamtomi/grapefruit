package grapefruit.command.completion;

import java.util.List;

public interface CompletionAccumulator {

    List<CommandCompletion> filterCompletions();
}
