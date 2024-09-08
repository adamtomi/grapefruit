package grapefruit.command.annotation.mapper.string;

import grapefruit.command.annotation.mapper.MappedBy;
import grapefruit.command.argument.mapper.standard.StringArgumentMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the argument is to be mapped by {@link StringArgumentMapper#quotable()} ()}
 */
@MappedBy(Quotable.NAME)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Quotable {
    String NAME = "__quotable__";
}
