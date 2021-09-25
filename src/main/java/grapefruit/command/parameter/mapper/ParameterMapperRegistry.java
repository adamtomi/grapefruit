package grapefruit.command.parameter.mapper;

import com.google.common.reflect.TypeToken;
import grapefruit.command.parameter.mapper.builtin.BooleanMapper;
import grapefruit.command.parameter.mapper.builtin.CharacterMapper;
import grapefruit.command.parameter.mapper.builtin.NumberMapper;
import grapefruit.command.parameter.mapper.builtin.StringMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class ParameterMapperRegistry<S> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<TypeToken<?>, ParameterMapper<S, ?>> defaultMappers = new HashMap<>();
    private final Map<String, ParameterMapper<S, ?>> namedMappers = new HashMap<>();

    public ParameterMapperRegistry() {
        registerDefaults();
    }

    private void registerDefaults() {
        internalRegister(new StringMapper<>());
        internalRegister(new CharacterMapper<>());
        internalRegister(new BooleanMapper<>());
        internalRegister(new NumberMapper<>(TypeToken.of(Byte.class), Byte::parseByte));
        internalRegister(new NumberMapper<>(TypeToken.of(Short.class), Short::parseShort));
        internalRegister(new NumberMapper<>(TypeToken.of(Integer.class), Integer::parseInt));
        internalRegister(new NumberMapper<>(TypeToken.of(Double.class), Double::parseDouble));
        internalRegister(new NumberMapper<>(TypeToken.of(Float.class), Float::parseFloat));
        internalRegister(new NumberMapper<>(TypeToken.of(Long.class), Long::parseLong));
    }

    private void internalRegister(final @NotNull ParameterMapper<S, ?> mapper) {
        this.defaultMappers.put(mapper.type(), mapper);
    }

    public void registerMapper(final @NotNull ParameterMapper<S, ?> mapper) {
        try {
            this.lock.lock();
            registerMapper0(mapper);
        } finally {
            this.lock.unlock();
        }
    }

    public void registerMappers(final @NotNull Collection<ParameterMapper<S, ?>> mappers) {
        try {
            this.lock.lock();
            mappers.forEach(this::registerMapper0);
        } finally {
            this.lock.unlock();
        }
    }

    private void registerMapper0(final @NotNull ParameterMapper<S, ?> mapper) {
        requireNonNull(mapper, "mapper cannot be null");
        final TypeToken<?> type = mapper.type();
        if (this.defaultMappers.containsKey(type)) {
            throw new IllegalStateException(String.format("Default parameterMapper with type %s is already registered", type.getType()));
        }

        this.defaultMappers.put(type, mapper);
    }

    public void registerNamedMapper(final @NotNull String name,
                                    final @NotNull ParameterMapper<S, ?> mapper) {
        try {
            this.lock.lock();
            registerNamedMapper0(name, mapper);
        } finally {
            this.lock.unlock();
        }
    }

    public void registerNamedMappers(final @NotNull Map<String, ParameterMapper<S, ?>> mappers) {
        try {
            this.lock.lock();
            mappers.forEach(this::registerNamedMapper0);
        } finally {
            this.lock.unlock();
        }
    }

    private void registerNamedMapper0(final @NotNull String name,
                                      final @NotNull ParameterMapper<S, ?> mapper) {
        requireNonNull(name, "name cannot be null");
        requireNonNull(mapper, "mapper cannot be null");
        if (this.namedMappers.containsKey(name)) {
            throw new IllegalStateException(format("ParameterMapper with name %s already registered", name));
        }

        this.namedMappers.put(name, mapper);
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull Optional<ParameterMapper<S, T>> findMapper(final @NotNull TypeToken<T> type) {
        try {
            this.lock.lock();
            final @Nullable ParameterMapper<S, T> mapper = (ParameterMapper<S, T>) this.defaultMappers.get(type);
            return Optional.ofNullable(mapper);
        } finally {
            this.lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull Optional<ParameterMapper<S, T>> findNamedMapper(final @NotNull String name) {
        try {
            this.lock.lock();
            final @Nullable ParameterMapper<S, T> mapper = (ParameterMapper<S, T>) this.namedMappers.get(name);
            return Optional.ofNullable(mapper);
        } finally {
            this.lock.unlock();
        }
    }
}
