package grapefruit.command.parameter;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public interface FlagParameter<S> extends CommandParameter<S> {
    TypeToken<Boolean> PRESENCE_FLAG_TYPE = TypeToken.of(Boolean.TYPE);
    Pattern FLAG_PATTERN = Pattern.compile("^-(-?)([a-zA-Z]\\S*)?$");

    @NotNull String flagName();

    char shorthand();

    @Override
    default boolean isFlag() {
        return true;
    }
}
