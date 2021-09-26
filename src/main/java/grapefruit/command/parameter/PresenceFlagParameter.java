package grapefruit.command.parameter;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.parameter.mapper.ParameterMappingException;
import grapefruit.command.util.AnnotationList;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;

import static java.util.Objects.requireNonNull;

public class PresenceFlagParameter<S> extends AbstractFlagParameter<S> {

    public PresenceFlagParameter(final @NotNull String flagName,
                                 final char shorthand,
                                 final @NotNull String name,
                                 final int index,
                                 final @NotNull AnnotationList modifiers) {
        super(
                flagName,
                shorthand,
                name,
                index,
                true,
                FlagParameter.PRESENCE_FLAG_TYPE,
                modifiers,
                new DummyParameterMapper<>(flagName)
        );
    }

    @Override
    public final boolean isMultiFlag() {
        return false;
    }

    @Override
    public @NotNull String toString() {
        return "PresenceFlagParameter[" +
                "name='" + flagName() + '\'' +
                ", shorthand='" + shorthand() + '\'' +
                ", index=" + index() +
                ", modifiers=" + modifiers() +
                ", mapper=" + mapper().getClass().getName() +
                ']';
    }

    private static final class DummyParameterMapper<S> implements ParameterMapper<S, Void> {
        private final String flagName;

        private DummyParameterMapper(final @NotNull String flagName) {
            this.flagName = requireNonNull(flagName, "flagName cannot be null");
        }

        @Override
        public @NotNull TypeToken<Void> type() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull Void map(final @NotNull CommandContext<S> context,
                                 final @NotNull Queue<CommandInput> args,
                                 final @NotNull AnnotationList modifiers) throws ParameterMappingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> context,
                                                     final @NotNull String currentArg,
                                                     final @NotNull AnnotationList modifiers) {
            return List.of(Miscellaneous.formatFlag(this.flagName));
        }
    }
}
