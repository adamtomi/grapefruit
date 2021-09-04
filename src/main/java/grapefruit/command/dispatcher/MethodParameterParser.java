package grapefruit.command.dispatcher;

import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.ParameterNode;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.parameter.modifier.Flag;
import grapefruit.command.parameter.modifier.Modifier;
import grapefruit.command.parameter.modifier.OptParam;
import grapefruit.command.parameter.modifier.Range;
import grapefruit.command.parameter.modifier.Resolver;
import grapefruit.command.parameter.modifier.Source;
import grapefruit.command.parameter.modifier.string.Greedy;
import grapefruit.command.parameter.modifier.string.Quotable;
import grapefruit.command.parameter.modifier.string.Regex;
import grapefruit.command.parameter.resolver.ParameterResolver;
import grapefruit.command.parameter.resolver.ResolverRegistry;
import grapefruit.command.util.AnnotationList;
import grapefruit.command.util.Miscellaneous;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

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
                && !Number.class.isAssignableFrom(GenericTypeReflector.erase(Miscellaneous.box(parameter.getType()).getType()))) {
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
    private final ResolverRegistry<?> resolverRegistry;

    MethodParameterParser(final @NotNull ResolverRegistry<?> resolverRegistry) {
        this.resolverRegistry = requireNonNull(resolverRegistry, "resolverRegistry cannot be null");
    }

    @SuppressWarnings("unchecked")
    @NotNull List<ParameterNode<S>> collectParameters(final @NotNull Method method) throws RuleViolationException {
        final List<ParameterNode<S>> parameters = new ArrayList<>();
        for (int i = 0; i < method.getParameters().length; i++) {
            final Parameter parameter = method.getParameters()[i];
            final AnnotationList annotations = new AnnotationList(parameter.getAnnotations());

            for (final Rule rule : this.rules) {
                rule.validate(method, parameter, annotations);
            }

            if (!annotations.has(Source.class)) {
                final String parameterName = annotations.find(Flag.class)
                        .map(Flag::value)
                        .orElse(parameter.getName());
                if (parameters.stream().anyMatch(x -> x.name().equalsIgnoreCase(parameterName))) {
                    throw new IllegalArgumentException(format("Duplicate parameters with name %s", parameterName));
                }

                final boolean isOptional = Stream.of(OptParam.class, Flag.class).anyMatch(annotations::has);
                final CommandParameter cmdParam = new CommandParameter(i,
                        TypeToken.get(parameter.getType()), annotations, isOptional);
                final ParameterResolver<S, ?> resolver;
                final Optional<Resolver> resolverAnnot = annotations.find(Resolver.class);

                if (resolverAnnot.isPresent()) {
                    final String name = resolverAnnot.get().value();
                    resolver = (ParameterResolver<S, ?>) this.resolverRegistry.findNamedResolver(name)
                            .orElseThrow(() -> new IllegalArgumentException(format("Could not find ParameterResolver with name %s", name)));
                } else {
                    resolver = (ParameterResolver<S, ?>) this.resolverRegistry.findResolver(cmdParam.type())
                            .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find ParameterResolver for type %s", cmdParam.type().getType())));
                }

                final ParameterNode<S> node = annotations.has(Flag.class)
                        ? cmdParam.type().getType().equals(Boolean.TYPE)
                        ? new StandardParameter.PresenceFlag<>(parameterName, cmdParam)
                        : new StandardParameter.ValueFlag<>(parameterName, resolver, cmdParam, parameter.getName())
                        : new StandardParameter<>(parameterName, resolver, cmdParam);
                parameters.add(node);
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
