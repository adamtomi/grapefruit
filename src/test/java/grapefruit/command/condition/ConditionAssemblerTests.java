package grapefruit.command.condition;

import grapefruit.command.dispatcher.CommandContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConditionAssemblerTests {
    private Method methodWithNoConditions;
    private Method methodWithASingleOrConditionGroup;
    private Method methodWithMultipleConditionGroups;

    @BeforeAll
    public void setUp() throws ReflectiveOperationException {
        final Class<?> clazz = MethodsContainer.class;
        this.methodWithNoConditions = clazz.getDeclaredMethod("methodWithNoConditions");
        this.methodWithASingleOrConditionGroup = clazz.getDeclaredMethod("methodWithASignleOrConditionGroup");
        this.methodWithMultipleConditionGroups = clazz.getDeclaredMethod("methodWithMultipleConditionGroups");
    }

    @Test
    public void constructCondition_empty() {
        final CommandConditionAssembler<Object> assembler = new CommandConditionAssembler<>(new CommandConditionRegistry<>());
        final Optional<CommandCondition<Object>> condition = assembler.constructCondition(this.methodWithNoConditions);
        assertTrue(condition.isEmpty());
    }

    @Test
    public void constructCondition_noConditionsRegistered() {
        final CommandConditionAssembler<Object> assembler = new CommandConditionAssembler<>(new CommandConditionRegistry<>());
        assertThrows(IllegalArgumentException.class, () -> assembler.constructCondition(this.methodWithASingleOrConditionGroup));
    }

    @Test
    public void constructCondition_singleConditionGroup() {
        final CommandConditionRegistry<Object> registry = new CommandConditionRegistry<>();
        final CommandConditionAssembler<Object> assembler = new CommandConditionAssembler<>(registry);
        Stream.of("a", "b", "c")
                .map(DummyCondition::new)
                .forEach(registry::registerCondition);

        assertDoesNotThrow(() -> assembler.constructCondition(this.methodWithASingleOrConditionGroup));
    }

    @Test
    public void constructCondition_multipleConditionGroups() {
        final CommandConditionRegistry<Object> registry = new CommandConditionRegistry<>();
        final CommandConditionAssembler<Object> assembler = new CommandConditionAssembler<>(registry);
        Stream.of("a", "b", "c", "d", "e", "f", "g", "h")
                .map(DummyCondition::new)
                .forEach(registry::registerCondition);

        assertDoesNotThrow(() -> assembler.constructCondition(this.methodWithMultipleConditionGroups));
    }

    private static final class DummyCondition implements CommandCondition<Object> {
        private final String id;

        DummyCondition(final String id) {
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

    private static final class MethodsContainer {

        public void methodWithNoConditions() {}

        @Condition({"a", "b", "c"})
        public void methodWithASignleOrConditionGroup() {}

        @Condition({"a", "b", "c"})
        @Condition("e")
        @Condition({"f", "g", "h"})
        public void methodWithMultipleConditionGroups() {}
    }
}
