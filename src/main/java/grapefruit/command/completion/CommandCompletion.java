package grapefruit.command.completion;

import java.util.List;

// TODO should we rename this to Completion? In that case the current Completion interface
// would have to be renamed to something else. Perhaps CompletionEntry?
public interface CommandCompletion {

    // TODO rename?
    List<Completion> completions();

    static CommandCompletion concat(final CommandCompletion... completions) {
        // TODO
        throw new UnsupportedOperationException("Not implemented yet");
    };

    static CommandCompletion none() {
        // TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
