package grapefruit.command.parameter.mapper.builtin;

import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.Template;
import grapefruit.command.parameter.mapper.AbstractParamterMapper;
import grapefruit.command.parameter.mapper.ParameterMappingException;
import grapefruit.command.util.AnnotationList;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;

public class CharacterMapper<S> extends AbstractParamterMapper<S, Character> {

    public CharacterMapper() {
        super(TypeToken.get(Character.class));
    }

    @Override
    public @NotNull Character map(final @NotNull S source,
                                  final @NotNull Queue<CommandInput> args,
                                  final @NotNull AnnotationList modifiers) throws ParameterMappingException {
        final String input = args.element().rawArg();
        if (input.length() != 1) {
            throw new ParameterMappingException(Message.of(
                    MessageKeys.INVALID_CHARACTER_VALUE,
                    Template.of("{input}", input)
            ));
        }

        return input.charAt(0);
    }
}
