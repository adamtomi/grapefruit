package grapefruit.command.parameter.resolver;

import grapefruit.command.parameter.resolver.builtin.BooleanResolver;
import grapefruit.command.parameter.resolver.builtin.NumberResolver;
import grapefruit.command.parameter.resolver.builtin.StringResolver;
import grapefruit.command.util.Miscellaneous;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class ResolverRegistry<S> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<TypeToken<?>, ParameterResolver<S, ?>> defaultResolvers = new HashMap<>();
    private final Map<String, ParameterResolver<S, ?>> namedResolvers = new HashMap<>();

    public ResolverRegistry() {
        registerDefaults();
    }

    private void registerDefaults() {
        internalRegister(new StringResolver<>());
        internalRegister(new BooleanResolver<>());
        internalRegister(new NumberResolver<>(TypeToken.get(Byte.class), Byte::parseByte));
        internalRegister(new NumberResolver<>(TypeToken.get(Short.class), Short::parseShort));
        internalRegister(new NumberResolver<>(TypeToken.get(Integer.class), Integer::parseInt));
        internalRegister(new NumberResolver<>(TypeToken.get(Double.class), Double::parseDouble));
        internalRegister(new NumberResolver<>(TypeToken.get(Float.class), Float::parseFloat));
        internalRegister(new NumberResolver<>(TypeToken.get(Long.class), Long::parseLong));
    }

    private void internalRegister(final @NotNull ParameterResolver<S, ?> resolver) {
        this.defaultResolvers.put(resolver.type(), resolver);
    }

    public void registerResolver(final @NotNull ParameterResolver<S, ?> resolver) {
        try {
            this.lock.lock();
            registerResolver0(resolver);
        } finally {
            this.lock.unlock();
        }
    }

    public void registerResolvers(final @NotNull Collection<ParameterResolver<S, ?>> resolvers) {
        try {
            this.lock.lock();
            resolvers.forEach(this::registerResolver0);
        } finally {
            this.lock.unlock();
        }
    }

    private void registerResolver0(final @NotNull ParameterResolver<S, ?> resolver) {
        requireNonNull(resolver, "resolver cannot be null");
        final TypeToken<?> type = Miscellaneous.box(resolver.type());
        if (this.defaultResolvers.containsKey(type)) {
            throw new IllegalStateException(String.format("Default arameterResolver with type %s is already registered", type.getType()));
        }

        this.defaultResolvers.put(type, resolver);
    }

    public void registerNamedResolver(final @NotNull String name,
                                      final @NotNull ParameterResolver<S, ?> resolver) {
        try {
            this.lock.lock();
            registerNamedResolver0(name, resolver);
        } finally {
            this.lock.unlock();
        }
    }

    public void registerNamedResolvers(final @NotNull Map<String, ParameterResolver<S, ?>> resolvers) {
        try {
            this.lock.lock();
            resolvers.forEach(this::registerNamedResolver0);
        } finally {
            this.lock.unlock();
        }
    }

    private void registerNamedResolver0(final @NotNull String name,
                                        final @NotNull ParameterResolver<S, ?> resolver) {
        requireNonNull(name, "name cannot be null");
        requireNonNull(resolver, "resolver cannot be null");
        if (this.namedResolvers.containsKey(name)) {
            throw new IllegalStateException(format("ParameterResolver with name %s already registered", name));
        }

        this.namedResolvers.put(name, resolver);
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull Optional<ParameterResolver<S, T>> findResolver(final @NotNull TypeToken<T> type) {
        try {
            this.lock.lock();
            final TypeToken<T> boxedType = Miscellaneous.box(type);
            final @Nullable ParameterResolver<S, T> resolver = (ParameterResolver<S, T>) this.defaultResolvers.get(boxedType);
            return Optional.ofNullable(resolver);
        } finally {
            this.lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull Optional<ParameterResolver<S, T>> findNamedResolver(final @NotNull String name) {
        try {
            this.lock.lock();
            final @Nullable ParameterResolver<S, T> resolver = (ParameterResolver<S, T>) this.namedResolvers.get(name);
            return Optional.ofNullable(resolver);
        } finally {
            this.lock.unlock();
        }
    }
}
