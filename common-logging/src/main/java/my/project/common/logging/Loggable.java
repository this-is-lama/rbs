package my.project.common.logging;

import org.slf4j.event.Level;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Логирует вызов метода с аргументами, результат и время выполнения.
 * Над классом — применяется ко всем его public-методам, над методом — переопределяет настройки класса.
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {

	Level level() default Level.DEBUG;
}
