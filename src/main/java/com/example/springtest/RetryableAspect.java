package com.example.springtest;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class RetryableAspect {

    @Around("@annotation(com.example.springtest.Retryable)")
    public Object retryMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method reatryableMethod = signature.getMethod();
        Retryable annotation = reatryableMethod.getAnnotation(Retryable.class);

        int retryCount = 0;

        Object proceed;
        Exception lazyException = null;

        while (retryCount <= annotation.maxAttempts()) {
            try {
                if (retryCount != 0) {
                    log.warn("Retry method...");
                    Thread.sleep(annotation.backoff());
                }
                // Proceed target method
                proceed = joinPoint.proceed();
                // Success case
                return proceed;
            } catch (Exception exception) {
                lazyException = exception;
                if (instanceContains(exception, annotation.include())) {
                    if (annotation.printStackTrace()) {
                        exception.printStackTrace();
                    }
                    retryCount++;
                } else {
                    throw exception;
                }
            }
        }

        Object target = joinPoint.getTarget();
        Class<?> retryableMethodReturnType = reatryableMethod.getReturnType();

        // Find @Recover method (@Retryable return type == @Recover return type)
        Method recoverMethod = findRecoverMethodWithSameReturnType(target, retryableMethodReturnType);

        if (recoverMethod != null) {
            log.info("Recover start");
            // The exception occurring here is not retried.
            return recoverMethod.invoke(target);
        } else {
            throw lazyException;
        }
    }

    private boolean instanceContains(Exception target, Class<? extends Exception>[] compareWiths) {
        for (Class<? extends Exception> ex : compareWiths) {
            if (target.getClass().isAssignableFrom(ex)) {
                return true;
            }
        }
        return false;
    }

    public Method findRecoverMethodWithSameReturnType(Object target, Class<?> retryableMethodReturnType) {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(target.getClass());

        // Find @Recover annotation
        for (Method method : methods) {
            if (method.isAnnotationPresent(Recover.class) &&
                    method.getReturnType().equals(retryableMethodReturnType)) {
                // If the return types are the same, then execute the recover logic.
                // Choose the first recover method
                return method;
            }
        }
        return null;
    }
}
