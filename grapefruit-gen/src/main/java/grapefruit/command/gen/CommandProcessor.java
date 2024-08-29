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
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

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
        Map<TypeElement, List<CommandDescriptor>> knownCommands = commandMethods.stream()
                .map(CommandDescriptor::create)
                .collect(groupingBy(
                        CommandDescriptor::parent,
                        mapping(
                                Function.identity(),
                                toList()
                        )
                ));

        return true;
    }
}
