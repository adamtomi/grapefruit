package grapefruit.command.runtime.dispatcher.config;

import grapefruit.command.runtime.argument.mapper.ArgumentMapper;
import grapefruit.command.runtime.argument.modifier.ArgumentModifier;
import grapefruit.command.runtime.dispatcher.CommandDispatcher;
import grapefruit.command.runtime.dispatcher.CommandRegistrationHandler;
import grapefruit.command.runtime.dispatcher.ExecutionListener;
import grapefruit.command.runtime.dispatcher.ExecutionStage;
import grapefruit.command.runtime.dispatcher.auth.CommandAuthorizer;
import grapefruit.command.runtime.dispatcher.condition.CommandCondition;
import grapefruit.command.runtime.util.Registry;
import grapefruit.command.runtime.util.key.Key;
import io.leangen.geantyref.TypeToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * This class is used to configure a {@link CommandDispatcher}.
 */
public abstract class DispatcherConfigurer {
    private final Registry<Key<?>, ArgumentMapper<?>> argumentMappers = Registry.create(Registry.DuplicateStrategy.reject());
    private final Registry<Key<?>, CommandCondition> conditions = Registry.create(Registry.DuplicateStrategy.reject());
    private final Registry<Key<?>, ArgumentModifier.Factory<?>> modifiers = Registry.create(Registry.DuplicateStrategy.reject());
    private final Registry<ExecutionStage, Queue<ExecutionListener>> listeners = Registry.create(Registry.DuplicateStrategy.reject());
    private CommandAuthorizer authorizer = null;
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
        root.listeners.merge(other.listeners);
        // Only copy properties that have been changed from their default values
        if (other.authorizer != null) root.authorizer = other.authorizer;
        if (other.registrationHandler != null) root.registrationHandler = other.registrationHandler;
    }

    /* Set default values if no custom value has been configured */
    private static void setDefaults(DispatcherConfigurer configurer) {
        if (configurer.authorizer == null) configurer.authorizer = CommandAuthorizer.ALWAYS_ALLOW;
        if (configurer.registrationHandler == null) configurer.registrationHandler = CommandRegistrationHandler.noop();
    }

    private void doConfigure() {
        if (this.configured) throw new IllegalStateException("This configurer has already been configured");
        configure();
        this.configured = true;
    }

    /**
     * Implementations need to override this method to configure their
     * {@link CommandDispatcher}.
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
    protected <T> MappingBuilder.Named<T> map(Class<T> type) {
        return map(TypeToken.get(type));
    }

    /**
     * @see this#map(Class)
     */
    protected <T> MappingBuilder.Named<T> map(TypeToken<T> type) {
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
                .collect(toMap(x -> Key.of(x.getClass()), ArgumentModifier.Factory::providing)));
    }

    /**
     * @see this#modifierFactories(Collection)
     */
    protected void modifierFactories(ArgumentModifier.Factory<?>... factories) {
        modifierFactories(Set.of(factories));
    }

    /**
     * Register the provided modifier factories.
     *
     * @param factories The factories to register
     */
    protected void modifierFactories(Collection<ArgumentModifier.Factory<?>> factories) {
        this.modifiers.storeEntries(factories.stream()
                .collect(toMap(x -> Key.of(x.getClass()), Function.identity())));
    }

    /**
     * Register the supplied {@link ExecutionListener listener}.
     *
     * @param stage The execution stage
     * @param listener The listener
     */
    protected void on(ExecutionStage stage, ExecutionListener listener) {
        if (!this.listeners.has(stage)) this.listeners.store(stage, new LinkedList<>());
        this.listeners.get(stage).orElseThrow().offer(listener);
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
    public Registry<Key<?>, ArgumentModifier.Factory<?>> modifiers() {
        return this.modifiers;
    }

    /**
     * Returns the registered listeners.
     * For internal use.
     */
    public Registry<ExecutionStage, Queue<ExecutionListener>> listeners() {
        return this.listeners;
    }
}