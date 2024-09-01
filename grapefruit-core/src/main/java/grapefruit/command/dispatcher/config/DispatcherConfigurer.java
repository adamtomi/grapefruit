package grapefruit.command.dispatcher.config;

public abstract class DispatcherConfigurer {

    /*
     * public void configure() {
     *     // Setup registration handler
     *     registrations().use(new MyCommandRegistartionHandler());
     *     // OR
     *     registration().on(State.REGISTERING, command -> {}).on(STATE.UNREGISTERING, command -> {});
     *     // Setup authorization handler
     *     authorize((perm, context) -> context.get(SENDER_KEY).hasPermission(perm));
     *     // Setup command listeners
     *     on(ExecutionStage.PRE_PROCESS).run(context -> {});
     *     on(ExecutionStage.POST_EXECUTION).run(context -> {});
     *
     *     // Setup argument mappers
     *     map(String.class).using(StringArgumentMapper.SINGLE);
     *     map(String.class).namedAs("greedy").using(StringArgumentMapper.GREEDY);
     *     map(Object.class).to(new Object());
     *     map(Integer.class).toLazy(() -> Integer.valueOf(2)); // Supplier
     *
     *     executor(() -> Executors.newCachedThreadPool());
     * }
     */
    
    /*
     * CommandDispatcher dispatcher = CommandDispatcher.using(new DefaultConfigurer(), new MyCustomConfigurer());
     */
}
