package grapefruit.command.gen;

import com.google.auto.service.AutoService;
import grapefruit.command.CommandDefinition;
import grapefruit.command.gen.descriptor.CommandDescriptor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.auto.common.MoreElements.asExecutable;

@AutoService(Processor.class)
@SupportedAnnotationTypes("grapefruit.command.CommandDefinition")
public class CommandProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> commandMethods = roundEnv.getElementsAnnotatedWith(CommandDefinition.class);
        Set<CommandDescriptor> commandDescriptors = commandMethods.stream()
                .map(this::process)
                .collect(Collectors.toSet());

        return true;
    }

    private CommandDescriptor process(Element method) {
        if (!method.getKind().equals(ElementKind.METHOD)) {
            throw new RuntimeException("Expected element to be a method");
        }

        return CommandDescriptor.create(asExecutable(method));
    }
}
