package grapefruit.command.message;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageTests {

    @ParameterizedTest
    @ValueSource(strings = {"some.message.key", "some-other.message-key"})
    public void messageKey_keyEquals(final String key) {
        final MessageKey constructedKey = MessageKey.of(key);
        assertEquals(key, constructedKey.key());
    }

    @ParameterizedTest
    @ValueSource(strings = {"someplaceholder", "{placeholder}", "%otherplaceholder%"})
    public void template_placeholderEquals(final String placeholder) {
        final Template template = Template.of(placeholder, "");
        assertEquals(placeholder, template.placeholder());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "fourth", " ", "..-"})
    public void template_replacementEquals(final String replacement) {
        final Template template = Template.of("", replacement);
        assertEquals(replacement, template.replacement());
    }

    @ParameterizedTest
    @CsvSource({
            "dispatcher.authorization-error,You lack permission '{permission}'",
            "parameter.invalid-boolean-value,'{input}' is not a valid boolean value. Options: {options}",
            "dispatcher.too-few-arguments,Too few arguments have been specified. Syntax: {syntax}",
            "parameter.string-regex-error,'{input}' has to match regex {regex}",
            "parameter.quoted-string-invalid-trailing-character,'{input}' has to end with '\"'"
    })
    public void messageProvider_provide_validInput(final String key, final String expected) {
        final MessageKey constructedKey = MessageKey.of(key);
        final MessageProvider provider = new DefaultMessageProvider();
        assertEquals(expected, provider.provide(constructedKey));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SOME_MESSAGE_KEY", "another-message-key", "yet.another.message.key"})
    public void messageProvider_provide_invalidInput(final String key) {
        final MessageKey constructedKey = MessageKey.of(key);
        final MessageProvider provider = new DefaultMessageProvider();
        assertThrows(IllegalArgumentException.class, () -> provider.provide(constructedKey));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first.key", "second.key", "third.key", "_.key"})
    public void message_keyEquals(final String key) {
        final MessageKey constructedKey = MessageKey.of(key);
        final Message message = Message.of(constructedKey);
        assertEquals(constructedKey, message.key());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first.key", "second.key", "third.key", "_.key"})
    public void message_templateContains(final String key) {
        final MessageKey constructedKey = MessageKey.of(key);
        final List<Template> templates = List.of(
                Template.of("{placeholder}", "hey"),
                Template.of("{otherPlaceholder}", "hello"),
                Template.of("{another-placeholder}", "hi")
        );
        final Message message = Message.of(constructedKey, templates.toArray(Template[]::new));
        assertTrue(contentEquals(templates, message.templates()));
    }

    @ParameterizedTest
    @CsvSource({
            "dispatcher.authorization-error,You lack permission '{permission}'",
            "parameter.invalid-boolean-value,'{input}' is not a valid boolean value. Options: {options}",
            "dispatcher.too-few-arguments,Too few arguments have been specified. Syntax: {syntax}",
            "parameter.string-regex-error,'{input}' has to match regex {regex}",
            "parameter.quoted-string-invalid-trailing-character,'{input}' has to end with '\"'"
    })
    public void message_get(final String key, final String expected) {
        final MessageKey constructedKey = MessageKey.of(key);
        final MessageProvider provider = new DefaultMessageProvider();
        final Template template = Template.of("{input}", "test");
        final Message message = Message.of(constructedKey, template);
        assertEquals(expected.replace(template.placeholder(), template.replacement()), message.get(provider));
    }

    @ParameterizedTest
    @CsvSource({
            "key-0,Some message",
            "key-1,'Some other message",
            "key-2,This is a message too",
    })
    public void defaultMessageProvider_register(final String key, final String message) {
        final DefaultMessageProvider messageProvider = new DefaultMessageProvider();
        final MessageKey constructedKey = MessageKey.of(key);
        messageProvider.register(constructedKey, message);
        assertEquals(message, messageProvider.provide(constructedKey));
    }

    private static boolean contentEquals(final List<Template> expected,
                                         final List<Template> result) {
        for (final Template template : expected) {
            if (!(result.contains(template)
                    && result.indexOf(template) == expected.indexOf(template))) {
                return false;
            }
        }

        return true;
    }
}
