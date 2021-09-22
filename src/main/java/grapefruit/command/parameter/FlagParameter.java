package grapefruit.command.parameter;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

public interface FlagParameter<S> extends CommandParameter<S> {
    TypeToken<Boolean> PRESENCE_FLAG_TYPE = TypeToken.get(Boolean.TYPE);

    @NotNull String flagName();

    @Override
    default boolean isFlag() {
        return true;
    }
}
