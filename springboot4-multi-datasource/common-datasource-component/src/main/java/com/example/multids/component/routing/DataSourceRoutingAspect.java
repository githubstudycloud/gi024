package com.example.multids.component.routing;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * 负责在方法调用前后切换数据源上下文。
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataSourceRoutingAspect {

    @Around("@within(com.example.multids.component.routing.UseDataSource) || "
            + "@annotation(com.example.multids.component.routing.UseDataSource)")
    public Object route(ProceedingJoinPoint joinPoint) throws Throwable {
        UseDataSource useDataSource = resolveAnnotation(joinPoint);
        if (useDataSource == null) {
            return joinPoint.proceed();
        }

        TenantRoutingDataSource.push(useDataSource.value());
        try {
            return joinPoint.proceed();
        } finally {
            TenantRoutingDataSource.pop();
        }
    }

    private UseDataSource resolveAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        UseDataSource annotation = AnnotatedElementUtils.findMergedAnnotation(method, UseDataSource.class);
        if (annotation != null) {
            return annotation;
        }
        return AnnotatedElementUtils.findMergedAnnotation(joinPoint.getTarget().getClass(), UseDataSource.class);
    }
}
