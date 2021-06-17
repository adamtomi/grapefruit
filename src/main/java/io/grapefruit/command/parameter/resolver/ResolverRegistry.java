package io.grapefruit.command.parameter.resolver;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;

public final class ResolverRegistry<S> {
    private final Map<String, ParameterResolver<S, ?>> resolvers = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void registerResolver(final @NotNull ParameterResolver<S, ?> resolver) {
        try {
            this.lock.lock();
            registerResolver0(resolver);
        } finally {
            this.lock.unlock();
        }
    }

    public void registerResolvers(final @NotNull ParameterResolver<S, ?>... resolvers) {
        registerResolvers(Set.of(resolvers));
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
        final String id = resolver.id();
        if (this.resolvers.containsKey(id)) {
            throw new IllegalStateException(format("ParameterResolver with id '%s' already registered", id));
        }

        this.resolvers.put(resolver.id(), resolver);
    }
}
