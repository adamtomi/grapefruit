package grapefruit.command.parameter;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

public interface FlagParameter<S> extends CommandParameter<S> {
    TypeToken<Boolean> PRESENCE_FLAG_TYPE = TypeToken.of(Boolean.TYPE);

    @NotNull String flagName();

    char shorthand();

    @Override
    default boolean isFlag() {
        return true;
    }
}
