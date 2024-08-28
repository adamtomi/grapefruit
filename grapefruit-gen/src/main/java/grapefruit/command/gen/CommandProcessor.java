package grapefruit.command.gen;

import com.google.auto.service.AutoService;
import grapefruit.command.CommandDefinition;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("grapefruit.command.CommandDefinition")
public class CommandProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> commandMethods = roundEnv.getElementsAnnotatedWith(CommandDefinition.class);
        return false;
    }
}
