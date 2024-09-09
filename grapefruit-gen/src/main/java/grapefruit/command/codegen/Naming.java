package grapefruit.command.codegen;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.function.Function;

public final class Naming {
    public static final Function<ExecutableElement, String> ACTION_METHOD_SUFFIX = method -> "%s$action".formatted(method.getSimpleName());
    public static final Function<ExecutableElement, String> ARGUMENTS_METHOD_SUFFIX = method -> "%s$arguments";
    public static final String COMMANDS = "commands";
    public static final Function<TypeElement, String> CONTAINER_CLASS_SUFFIX = clazz -> "%s_Container".formatted(clazz.getSimpleName());
    public static final String CONTEXT_PARAM = "context";
    public static final String FACTORY_SUFFIX = "_Factory";
    public static final String GENERATE_METHOD = "generate";
    public static final String INTERNAL_FACTORY_FIELD = "internalFactory";
    public static final String KEY_FIELD_SUFFIX = "_key";
    public static final String REFERENCE_PARAM = "reference";
    public static final String RESULT = "result";

    private Naming() {}
}
