package my.project.common.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggableAspect {

	@Around("@within(my.project.common.logging.Loggable) || @annotation(my.project.common.logging.Loggable)")
	public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(joinPoint.getTarget());
		Method method = AopUtils.getMostSpecificMethod(signature.getMethod(), targetClass);

		Logger log = LoggerFactory.getLogger(targetClass);
		Level level = resolveLevel(method, targetClass);

		if (!log.isEnabledForLevel(level)) {
			return joinPoint.proceed();
		}

		String methodName = method.getName();
		log.atLevel(level).log("Вызов {}({})", methodName,
				LogValueFormatter.formatArgs(signature.getParameterNames(), joinPoint.getArgs()));

		long start = System.nanoTime();
		try {
			Object result = joinPoint.proceed();
			if (method.getReturnType() == void.class) {
				log.atLevel(level).log("Завершён {} ({} мс)", methodName, elapsedMs(start));
			} else {
				log.atLevel(level).log("Завершён {} -> {} ({} мс)",
						methodName, LogValueFormatter.format(result), elapsedMs(start));
			}
			return result;
		} catch (Throwable ex) {
			log.atLevel(level).log("Ошибка в {}: {}: {} ({} мс)",
					methodName, ex.getClass().getSimpleName(), ex.getMessage(), elapsedMs(start));
			throw ex;
		}
	}

	private static Level resolveLevel(Method method, Class<?> targetClass) {
		Loggable loggable = AnnotatedElementUtils.findMergedAnnotation(method, Loggable.class);
		if (loggable == null) {
			loggable = AnnotatedElementUtils.findMergedAnnotation(targetClass, Loggable.class);
		}
		return loggable == null ? Level.DEBUG : loggable.level();
	}

	private static long elapsedMs(long start) {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
	}
}
