package grapefruit.command.dispatcher.config;

import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.argument.modifier.ArgumentModifier;
import grapefruit.command.argument.modifier.ContextualModifier;
import grapefruit.command.dispatcher.CommandRegistrationHandler;
import grapefruit.command.dispatcher.auth.CommandAuthorizer;
import grapefruit.command.dispatcher.condition.CommandCondition;
import grapefruit.command.util.Registry;
import grapefruit.command.util.key.Key;
import io.leangen.geantyref.TypeToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * This class is used to configure a {@link grapefruit.command.dispatcher.CommandDispatcher}.
 */
public abstract class DispatcherConfigurer {
    private final Registry<Key<?>, ArgumentMapper<?>> argumentMappers = Registry.create(Registry.DuplicateStrategy.reject());
    private final Registry<Key<?>, CommandCondition> conditions = Registry.create(Registry.DuplicateStrategy.reject());
    private final Registry<Key<?>, Function<ContextualModifier.Context, ArgumentModifier<?>>> modifiers = Registry.create(Registry.DuplicateStrategy.reject());
    private CommandAuthorizer authorizer = null;
    private Supplier<Executor> executor = null;
    private CommandRegistrationHandler registrationHandler = null;
    private boolean configured = false;

    /**
     * Collects all configuration details held by the passed in configurers
     * and returns a configurer instance holding these details merged together.
     *
     * @param configurers The configurers to merge
     * @return The configurer holding the merged details
     */
    public static DispatcherConfigurer merge(DispatcherConfigurer... configurers) {
        requireNonNull(configurers, "configurers cannot be null");
        // At least one configurer is required
        if (configurers.length == 0) throw new IllegalStateException("At least one configurer is required");

        // Data held by other configurers will be copied to "root"
        DispatcherConfigurer root = configurers[0];
        root.doConfigure();

        if (configurers.length > 1) {
            // Merge configurers into root
            for (DispatcherConfigurer configurer : Arrays.copyOfRange(configurers, 1, configurers.length)) {
                configurer.doConfigure();
                merge0(root, configurer);
            }
        }

        // Set default values if needed
        setDefaults(root);
        return root;
    }

    private static void merge0(DispatcherConfigurer root, DispatcherConfigurer other) {
        /*
         * Merge argument mappers. If both configurers have registrations for the
         * same type, the one held by "other" will take precedence.
         */
        root.argumentMappers.merge(other.argumentMappers);
        root.conditions.merge(other.conditions);
        root.modifiers.merge(other.modifiers);
        // Only copy properties that have been changed from their default values
        if (other.authorizer != null) root.authorizer = other.authorizer;
        if (other.executor != null) root.executor = other.executor;
        if (other.registrationHandler != null) root.registrationHandler = other.registrationHandler;
    }

    /* Set default values if no custom value has been configured */
    private static void setDefaults(DispatcherConfigurer configurer) {
        if (configurer.authorizer == null) configurer.authorizer = CommandAuthorizer.ALWAYS_ALLOW;
        if (configurer.executor == null) configurer.executor = () -> Runnable::run;
        if (configurer.registrationHandler == null) configurer.registrationHandler = CommandRegistrationHandler.noop();
    }

    private void doConfigure() {
        if (this.configured) throw new IllegalStateException("This configurer has already been configured");
        configure();
        this.configured = true;
    }

    /**
     * Implementations need to override this method to configure their
     * {@link grapefruit.command.dispatcher.CommandDispatcher}.
     */
    public abstract void configure();

    // Configuration methods

    /**
     * Sets the {@link CommandAuthorizer}.
     *
     * @param authorizer The authorizer to use
     */
    protected void authorize(CommandAuthorizer authorizer) {
        this.authorizer = requireNonNull(authorizer, "authorizer cannot be null");
    }

    /**
     * Sets the {@link Executor} used by the {@link grapefruit.command.dispatcher.CommandDispatcher}.
     *
     * @param executor Supplier returning the executor to use
     */
    protected void executor(Supplier<Executor> executor) {
        this.executor = requireNonNull(executor, "executor cannot be null");
    }

    /**
     * Returns a new {@link RegistrationBuilder} instance to manage
     * command registrations.
     *
     * @return The created builder instance
     */
    protected RegistrationBuilder registrations() {
        return new RegistrationBuilderImpl(value -> this.registrationHandler = value);
    }

    /**
     * Returns a new {@link MappingBuilder} instance to register
     * {@link ArgumentMapper} instances to specific types.
     *
     * @param <T> The argument type as generic
     * @param type The type of the argument
     * @return The new builer instance
     */
    protected <T> MappingBuilder<T> map(Class<T> type) {
        return map(TypeToken.get(type));
    }

    /**
     * @see this#map(Class)
     */
    protected <T> MappingBuilder<T> map(TypeToken<T> type) {
        return new MappingBuilderImpl<>(type, this.argumentMappers::store);
    }

    /**
     * @see this#conditions(Collection)
     */
    protected void conditions(CommandCondition... conditions) {
        conditions(Set.of(conditions));
    }

    /**
     * Registers the supplied conditions.
     *
     * @param conditions The conditions to register
     */
    protected void conditions(Collection<CommandCondition> conditions) {
        this.conditions.storeEntries(conditions.stream().collect(toMap(x -> Key.of(x.getClass()), Function.identity())));
    }

    /**
     * @see this#modifiers(Collection)
     */
    protected void modifiers(ArgumentModifier<?>... modifiers) {
        modifiers(Set.of(modifiers));
    }

    /**
     * Registers the provided argument modifiers.
     *
     * @param modifiers The modifiers to register
     */
    protected void modifiers(Collection<ArgumentModifier<?>> modifiers) {
        this.modifiers.storeEntries(modifiers.stream()
                // In this instance, ctx will always be null
                .collect(toMap(x -> Key.of(x.getClass()), x -> ctx -> x)));
    }

    /**
     * @see this#modifierFactories(Collection)
     */
    protected void modifierFactories(ContextualModifier.Factory<?>... factories) {
        modifierFactories(Set.of(factories));
    }

    /**
     * Register the provided modifier factories.
     *
     * @param factories The factories to register
     */
    protected void modifierFactories(Collection<ContextualModifier.Factory<?>> factories) {
        this.modifiers.storeEntries(factories.stream()
                .collect(toMap(x -> Key.of(x.getClass()), x -> x::createFromContext)));
    }

    // Getters

    /**
     * Returns the configured {@link CommandAuthorizer}.
     * For internal use.
     */
    public CommandAuthorizer authorizer() {
        return this.authorizer;
    }

    /**
     * Returns the configured {@link Executor} factory.
     * For internal use.
     */
    public Supplier<Executor> executor() {
        return this.executor;
    }

    /**
     * Returns the configured {@link CommandRegistrationHandler}.
     * For internal use.
     */
    public CommandRegistrationHandler registrationHandler() {
        return this.registrationHandler;
    }

    /**
     * Returns the configured argument mapper registry.
     * For internal use.
     */
    public Registry<Key<?>, ArgumentMapper<?>> argumentMappers() {
        return this.argumentMappers;
    }

    /**
     * Returns the registered conditions.
     * For internal use.
     */
    public Registry<Key<?>, CommandCondition> conditions() {
        return this.conditions;
    }

    /**
     * Returns the registered modifiers
     * For internal use.
     */
    public Registry<Key<?>, Function<ContextualModifier.Context, ArgumentModifier<?>>> modifiers() {
        return this.modifiers;
    }
}
