package grapefruit.command.binding;

import com.google.common.reflect.TypeToken;

public interface BindingSource {

    <T> BindingBuilder.Annotated<T> bind(Class<T> clazz);

    <T> BindingBuilder.Annotated<T> bind(TypeToken<T> type);
}
