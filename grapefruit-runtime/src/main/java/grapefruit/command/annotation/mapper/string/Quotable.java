package grapefruit.command.annotation.mapper.string;

import grapefruit.command.annotation.mapper.MappedBy;
import grapefruit.command.argument.mapper.builtin.StringArgumentMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the argument is to be mapped by {@link StringArgumentMapper#quotable()} ()}
 */
@MappedBy(StringArgumentMapper.QUOTABLE_NAME)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface Quotable {

}
