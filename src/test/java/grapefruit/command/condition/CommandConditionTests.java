package grapefruit.command.condition;

import grapefruit.command.dispatcher.CommandContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandConditionTests {

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "some-id", "c", "other_id", "JustAnotherId"})
    public void conditionRegistry_find_alwaysFail(final String id) {
        final CommandConditionRegistry<Object> registry = new CommandConditionRegistry<>();
        final Optional<CommandCondition<Object>> condition = registry.findCondition(id);
        assertTrue(condition.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "some-id", "c", "other_id", "JustAnotherId"})
    public void conditionRegistry_find_alwaysPass(final String id) {
        final CommandConditionRegistry<Object> registry = new CommandConditionRegistry<>();
        registry.registerCondition(new AlwaysPassingCondition(id));
        final Optional<CommandCondition<Object>> condition = registry.findCondition(id);
        assertTrue(condition.isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "some-id", "c", "other_id", "JustAnotherId"})
    public void conditionRegistry_register_alwaysFail_idAlreadyPresent(final String id) {
        final CommandConditionRegistry<Object> registry = new CommandConditionRegistry<>();
        registry.registerCondition(new AlwaysPassingCondition(id));
        assertThrows(IllegalStateException.class, () -> registry.registerCondition(new AlwaysFailingCondition(id)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"$$$", "////", "$id", "§hi", "hello ther"})
    public void conditionRegistry_registre_alwaysFail_invalidId(final String id) {
        final CommandConditionRegistry<Object> registry = new CommandConditionRegistry<>();
        assertThrows(IllegalArgumentException.class, () -> registry.registerCondition(new AlwaysPassingCondition(id)));
    }

    @Test
    public void andCondition_testFail() {
        final CommandConditionRegistry<Object> reg = new CommandConditionRegistry<>();
        final CommandContext<Object> context = dummyContext(new Object());
        final CommandCondition<Object> failingCondition = new AlwaysFailingCondition("a");
        final CommandCondition<Object> passingCondition = new AlwaysPassingCondition("b");

        reg.registerCondition(failingCondition);
        reg.registerCondition(passingCondition);
        final AndCondition<Object> condition = new AndCondition<>(List.of(failingCondition, passingCondition));
        assertThrows(ConditionFailedException.class, () -> condition.test(context));
    }

    @Test
    public void andCondition_testPass() {
        final CommandConditionRegistry<Object> reg = new CommandConditionRegistry<>();
        final CommandContext<Object> context = dummyContext(new Object());
        final CommandCondition<Object> passingCondition01 = new AlwaysPassingCondition("a");
        final CommandCondition<Object> passingCondition02 = new AlwaysPassingCondition("b");

        reg.registerCondition(passingCondition01);
        reg.registerCondition(passingCondition02);
        final AndCondition<Object> condition = new AndCondition<>(List.of(passingCondition01, passingCondition02));
        assertDoesNotThrow(() -> condition.test(context));
    }

    @Test
    public void orCondition_testPass() {
        final CommandConditionRegistry<Object> reg = new CommandConditionRegistry<>();
        final CommandContext<Object> context = dummyContext(new Object());
        final List<CommandCondition<Object>> allConditions = Stream.of("first", "second", "third", "fourth")
                .map(AlwaysFailingCondition::new).distinct().collect(Collectors.toList());
        final CommandCondition<Object> passingCondition = new AlwaysPassingCondition("a");
        allConditions.add(passingCondition);

        allConditions.forEach(reg::registerCondition);
        final OrCondition<Object> condition = new OrCondition<>(allConditions);
        assertDoesNotThrow(() -> condition.test(context));
    }

    @Test
    public void orCondition_testFail() {
        final CommandConditionRegistry<Object> reg = new CommandConditionRegistry<>();
        final CommandContext<Object> context = dummyContext(new Object());
        final List<CommandCondition<Object>> allConditions = Stream.of("first", "second", "third", "fourth")
                .map(AlwaysFailingCondition::new).distinct().collect(Collectors.toList());

        allConditions.forEach(reg::registerCondition);
        final OrCondition<Object> condition = new OrCondition<>(allConditions);
        assertThrows(ConditionFailedException.class, () -> condition.test(context));
    }

    private static <S> CommandContext<S> dummyContext(final S source) {
        return CommandContext.create(source, "", List.of());
    }

    private static final class AlwaysFailingCondition implements CommandCondition<Object> {
        private final String id;

        AlwaysFailingCondition(final String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return this.id;
        }

        @Override
        public void test(final CommandContext<Object> context) throws ConditionFailedException {
            throw new ConditionFailedException(this.id, context);
        }
    }

    private static final class AlwaysPassingCondition implements CommandCondition<Object> {
        private final String id;

        AlwaysPassingCondition(final String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return this.id;
        }

        @Override
        public void test(final CommandContext<Object> context) throws ConditionFailedException {
            // do nothing
        }
    }
}
