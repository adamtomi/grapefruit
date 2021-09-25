package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import grapefruit.command.parameter.PresenceFlagParameter;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.parameter.ValueFlagParameter;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.parameter.mapper.ParameterMapperRegistry;
import grapefruit.command.parameter.modifier.Flag;
import grapefruit.command.parameter.modifier.Mapper;
import grapefruit.command.parameter.modifier.Modifier;
import grapefruit.command.parameter.modifier.OptParam;
import grapefruit.command.parameter.modifier.Range;
import grapefruit.command.parameter.modifier.Source;
import grapefruit.command.parameter.modifier.string.Greedy;
import grapefruit.command.parameter.modifier.string.Quotable;
import grapefruit.command.parameter.modifier.string.Regex;
import grapefruit.command.util.AnnotationList;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("unchecked")
final class MethodParameterParser<S> {
    private static final Rule GREEDY_AND_QUOTABLE = (method, param, annotations) -> {
        if (annotations.has(Greedy.class) && annotations.has(Quotable.class)) {
            throw new RuleViolationException(format("Both @Greedy and @Quotable annotation used on parameter %s", param));
        }
    };
    private static final Rule GREEDY_QUOTABLE_REGEX_INVALID_TYPE = (method, parameter, annotations) -> {
        if (!String.class.isAssignableFrom(parameter.getType())
                && (annotations.has(Greedy.class) || annotations.has(Quotable.class) || annotations.has(Regex.class))) {
            throw new RuleViolationException("@Greedy, @Quotable and @Regex annotations are only allowed on strings");
        }
    };
    private static final Rule GREEDY_MUST_BE_LAST = (method, parameter, annotations) -> {
        if (annotations.has(Greedy.class) && !method.getParameters()[method.getParameters().length - 1].equals(parameter)) {
            throw new RuleViolationException("Parameter annotated with @Greedy must be the last parameter");
        }
    };
    private static final Rule RANGE_INVALID_TYPE = (method, parameter, annotations) -> {
        if (annotations.has(Range.class)
                && !Number.class.isAssignableFrom(Miscellaneous.box(parameter.getType()))) {
            throw new RuleViolationException("@Range annotation is only allowed on numbers");
        }
    };
    private static final Rule SOURCE_HAS_MORE_ANNOTATIONS = (method, parameter, annotations) -> {
        if (annotations.has(Source.class) && annotations.elements().size() > 1) {
            throw new RuleViolationException("Parameter annotated with @Source must not have any more annotations");
        }
    };
    private static final Rule SOURCE_NTH_PARAMETER = (method, param, annotations) -> {
        if (annotations.has(Source.class) && !method.getParameters()[0].equals(param)) {
            throw new RuleViolationException("Command source must be the first parameter");
        }
    };
    private static final Rule UNRECOGNIZED_ANNOTATION = (method, parameter, annotations) -> {
        for (final Annotation each : annotations.elements()) {
            if (!each.annotationType().isAnnotationPresent(Modifier.class)) {
                throw new RuleViolationException(format("Invalid annotation (%s) on parameter %s", each, parameter));
            }
        }
    };

    private final Set<Rule> rules = Set.of(
            GREEDY_AND_QUOTABLE,
            GREEDY_QUOTABLE_REGEX_INVALID_TYPE,
            GREEDY_MUST_BE_LAST,
            RANGE_INVALID_TYPE,
            SOURCE_HAS_MORE_ANNOTATIONS,
            SOURCE_NTH_PARAMETER,
            UNRECOGNIZED_ANNOTATION
    );
    private final ParameterMapperRegistry<?> mapperRegistry;

    MethodParameterParser(final @NotNull ParameterMapperRegistry<?> mapperRegistry) {
        this.mapperRegistry = requireNonNull(mapperRegistry, "mapperRegistry cannot be null");
    }

    @NotNull List<CommandParameter<S>> collectParameters(final @NotNull Method method) throws RuleViolationException {
        final List<CommandParameter<S>> parameters = new ArrayList<>();
        for (int i = 0; i < method.getParameters().length; i++) {
            final Parameter parameter = method.getParameters()[i];
            final AnnotationList annotations = new AnnotationList(parameter.getAnnotations());

            for (final Rule rule : this.rules) {
                rule.validate(method, parameter, annotations);
            }

            if (!annotations.has(Source.class)) {
                final boolean isFlag = annotations.has(Flag.class);
                final boolean isOptional = isFlag || annotations.has(OptParam.class);
                final TypeToken<?> type = TypeToken.of(Miscellaneous.box(parameter.getAnnotatedType().getType()));
                final ParameterMapper<S, ?> mapper;
                final Optional<Mapper> mapperAnnot = annotations.find(Mapper.class);

                if (mapperAnnot.isPresent()) {
                    final String name = mapperAnnot.get().value();
                    mapper = (ParameterMapper<S, ?>) this.mapperRegistry.findNamedMapper(name)
                            .orElseThrow(() -> new IllegalArgumentException(format("Could not find ParameterMapper with name %s", name)));
                } else {
                    mapper = (ParameterMapper<S, ?>) this.mapperRegistry.findMapper(type)
                            .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find ParameterMapper for type %s",
                                   type.getType())));
                }

                final String paramName = parameter.getName();
                final CommandParameter<S> cmdParam;
                if (isFlag) {
                    final String flagName = annotations.find(Flag.class)
                            .map(Flag::value)
                            .orElseThrow();
                    cmdParam = type.equals(FlagParameter.PRESENCE_FLAG_TYPE)
                            ? new PresenceFlagParameter<>(flagName, paramName, i, annotations)
                            : new ValueFlagParameter<>(flagName, paramName, i, type, annotations, mapper);
                } else {
                    cmdParam = new StandardParameter<>(paramName, i, isOptional, type, annotations, mapper);
                }

                parameters.add(cmdParam);
            }
        }

        return parameters;
    }

    @FunctionalInterface
    interface Rule {

        void validate(final @NotNull Method method, final @NotNull Parameter parameter, final @NotNull AnnotationList annotations)
                throws RuleViolationException;
    }

    static final class RuleViolationException extends Exception {
        @Serial
        private static final long serialVersionUID = -5242635147261283116L;

        private RuleViolationException(final @NotNull String message) {
            super(message);
        }
    }
}
