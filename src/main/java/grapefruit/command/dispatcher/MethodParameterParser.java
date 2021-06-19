package grapefruit.command.dispatcher;

import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.modifier.ParamModifier;
import grapefruit.command.parameter.modifier.Range;
import grapefruit.command.parameter.modifier.Resolver;
import grapefruit.command.parameter.modifier.Source;
import grapefruit.command.parameter.modifier.string.Greedy;
import grapefruit.command.parameter.modifier.string.Quoted;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

final class MethodParameterParser {
    private static final Rule GREEDY_AND_QUOTED = (method, param, annotations) -> {
        if (annotations.has(Greedy.class) && annotations.has(Quoted.class)) {
            throw new RuleViolationException(format("Both @Greedy and @Quoted annotation used on parameter %s", param));
        }
    };
    private static final Rule GREEDY_QUOTED_INVALID_TYPE = (method, parameter, annotations) -> {
        if (!String.class.isAssignableFrom(parameter.getType())
                && (annotations.has(Greedy.class) || annotations.has(Quoted.class))) {
            throw new RuleViolationException("@Greedy and @Quoted annotations are only allowed on strings");
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
            throw new RuleViolationException("Parameter annotated with @Source must not have more annotations");
        }
    };
    private static final Rule SOURCE_NTH_PARAMETER = (method, param, annotations) -> {
        if (annotations.has(Source.class) && !method.getParameters()[0].equals(param)) {
            throw new RuleViolationException("Command source must be the first parameter");
        }
    };
    private static final Rule UNRECOGNIZED_ANNOTATION = (method, parameter, annotations) -> {
        for (final Annotation each : annotations.elements()) {
            if (!each.annotationType().isAnnotationPresent(ParamModifier.class)) {
                throw new RuleViolationException(format("Invalid annotation (%s) on parameter %s", each, parameter));
            }
        }
    };

    private final Set<Rule> rules = Set.of(
            GREEDY_AND_QUOTED,
            GREEDY_QUOTED_INVALID_TYPE,
            RANGE_INVALID_TYPE,
            SOURCE_HAS_MORE_ANNOTATIONS,
            SOURCE_NTH_PARAMETER,
            UNRECOGNIZED_ANNOTATION
    );
    private final ResolverRegistry<?> resolverRegistry;

    MethodParameterParser(final @NotNull ResolverRegistry<?> resolverRegistry) {
        this.resolverRegistry = requireNonNull(resolverRegistry, "resolverRegistry cannot be null");
    }

    @NotNull List<ParameterNode> collectParameters(final @NotNull Method method) throws RuleViolationException {
        final List<ParameterNode> parameters = new ArrayList<>();
        for (final Parameter parameter : method.getParameters()) {
            final AnnotationList annotations = new AnnotationList(parameter.getAnnotations());

            for (final Rule rule : this.rules) {
                rule.validate(method, parameter, annotations);
            }

            final CommandParameter cmdParam = new CommandParameter(TypeToken.get(parameter.getType()), annotations);
            final ParameterResolver<?, ?> resolver;
            final Optional<Resolver> resolverAnnot = annotations.find(Resolver.class);
            if (resolverAnnot.isPresent() && !annotations.has(Source.class)) {
                final String name = resolverAnnot.get().value();
                resolver = this.resolverRegistry.findNamedResolver(name)
                        .orElseThrow(() -> new IllegalArgumentException(format("Could not find ParameterResolver with name %s", name)));
            } else {
                resolver = this.resolverRegistry.findResolver(cmdParam.type())
                        .orElseThrow(() -> new IllegalArgumentException(format("Could not find ParameterResolver for type %s", cmdParam.type().getType())));
            }

            parameters.add(new ParameterNode(resolver, cmdParam));
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
