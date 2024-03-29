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
import org.jetbrains.annotations.Nullable;

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
    private static final Rule FLAG_OPTIONAL = (method, parameter, annotations) -> {
        if (annotations.has(Flag.class) && annotations.has(OptParam.class)) {
            throw new RuleViolationException(format("Flags are optional by default, so they may not be annotated with @OptParam (%s)", parameter));
        }
    };
    private static final Rule CONTEXT_HAS_TO_BE_LAST = (method, parameter, annotations) -> {
         if (isCmdContext(parameter) && !method.getParameters()[method.getParameters().length - 1].equals(parameter)) {
             throw new RuleViolationException("CommandContext may only be the last parameter");
         }
    };

    private final Set<Rule> rules = Set.of(
            GREEDY_AND_QUOTABLE,
            GREEDY_QUOTABLE_REGEX_INVALID_TYPE,
            GREEDY_MUST_BE_LAST,
            RANGE_INVALID_TYPE,
            SOURCE_HAS_MORE_ANNOTATIONS,
            SOURCE_NTH_PARAMETER,
            UNRECOGNIZED_ANNOTATION,
            FLAG_OPTIONAL,
            CONTEXT_HAS_TO_BE_LAST
    );
    private final ParameterMapperRegistry<?> mapperRegistry;

    MethodParameterParser(final @NotNull ParameterMapperRegistry<?> mapperRegistry) {
        this.mapperRegistry = requireNonNull(mapperRegistry, "mapperRegistry cannot be null");
    }

    @NotNull ParseResult<S> collectParameters(final @NotNull Method method) throws RuleViolationException {
        final List<CommandParameter<S>> parameters = new ArrayList<>();
        final Parameter[] methodParams = method.getParameters();
        final @Nullable TypeToken<?> commandSourceType = methodParams.length > 0 && methodParams[0].isAnnotationPresent(Source.class)
                ? typeTokenOf(methodParams[0])
                : null;
        final boolean requiresContext = methodParams.length > 0
                && isCmdContext(methodParams[methodParams.length - 1]);

        for (final Parameter parameter : methodParams) {
            final AnnotationList annotations = new AnnotationList(parameter.getAnnotations());

            for (final Rule rule : this.rules) {
                rule.validate(method, parameter, annotations);
            }

            if (annotations.has(Source.class) || isCmdContext(parameter)) {
                continue;
            }

            final boolean isFlag = annotations.has(Flag.class);
            final boolean isOptional = isFlag || annotations.has(OptParam.class);
            final TypeToken<?> type = typeTokenOf(parameter);
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
                final Flag flagDef = annotations.find(Flag.class).orElseThrow();
                final String flagName = flagDef.value();
                final char shorthand = flagDef.shorthand();
                final List<FlagParameter<S>> existingFlags = parameters.stream()
                        .filter(CommandParameter::isFlag)
                        .map(x -> (FlagParameter<S>) x)
                        .toList();
                if (existingFlags.stream().anyMatch(x -> x.flagName().equalsIgnoreCase(flagName))) {
                    throw new IllegalStateException(format("Flag with name '%s' already registered", flagName));
                }

                if (existingFlags.stream().filter(Miscellaneous::shorthandNotEmpty)
                        .anyMatch(x -> x.shorthand() == shorthand)) {
                    throw new IllegalStateException(format("Flag with shorthand '%s' already registered", shorthand));
                }

                cmdParam = type.equals(FlagParameter.PRESENCE_FLAG_TYPE)
                        ? new PresenceFlagParameter<>(flagName, shorthand, paramName, annotations)
                        : new ValueFlagParameter<>(flagName, shorthand, paramName, type, annotations, mapper);
            } else {
                cmdParam = new StandardParameter<>(parameter.getName(), isOptional, type, annotations, mapper);
            }

            final String actualName = cmdParam.name();
            if (parameters.stream().anyMatch(x -> x.name().equalsIgnoreCase(actualName))) {
                throw new IllegalStateException(format("Parameter with name '%s' already registerd", actualName));
            }

            parameters.add(cmdParam);
        }

        return new ParseResult<>(parameters, commandSourceType, requiresContext);
    }

    private static @NotNull TypeToken<?> typeTokenOf(final @NotNull Parameter parameter) {
        return TypeToken.of(parameter.getAnnotatedType().getType());
    }

    private static boolean isCmdContext(final @NotNull Parameter parameter) {
        return CommandContext.class.isAssignableFrom(parameter.getType());
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

    static final record ParseResult<S>(@NotNull List<CommandParameter<S>> parameters,
                                       @Nullable TypeToken<?> commandSourceType,
                                       boolean requiresContext) {}
}
