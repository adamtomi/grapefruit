package grapefruit.command.parameter;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public interface FlagParameter<S> extends CommandParameter<S> {
    TypeToken<Boolean> PRESENCE_FLAG_TYPE = TypeToken.get(Boolean.TYPE);
    Pattern FLAG_PATTERN = Pattern.compile("^--(\\S+)$");

    @NotNull String flagName();

    @Override
    default boolean isFlag() {
        return true;
    }
}
