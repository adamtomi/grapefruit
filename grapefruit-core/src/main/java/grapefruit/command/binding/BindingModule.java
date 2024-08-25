package grapefruit.command.binding;

import com.google.common.reflect.TypeToken;

public abstract class BindingModule implements BindingSource {

    public abstract void setup();

    @Override
    public final <T> BindingBuilder.Annotated<T> bind(Class<T> clazz) {
        return bind(TypeToken.of(clazz));
    }

    @Override
    public final <T> BindingBuilder.Annotated<T> bind(TypeToken<T> type) {
        throw new UnsupportedOperationException();
    }
}
