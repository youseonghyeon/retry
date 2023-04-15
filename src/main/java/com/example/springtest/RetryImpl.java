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
public class RetryImpl {

    @Around("@annotation(com.example.springtest.Retryable)")
    public Object retryMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Retryable annotation = method.getAnnotation(Retryable.class);

        int retryCount = 0;
        Class<? extends Exception>[] include = annotation.include();
        int backoff = annotation.backoff();

        Object proceed = null;

        while (retryCount <= annotation.maxAttempts()) {
            try {
                if (retryCount != 0) {
                    log.warn("Retry method...");
                    Thread.sleep(backoff);
                }
                // Proceed target method
                proceed = joinPoint.proceed();

            } catch (Exception exception) {
                if (instanceContains(exception, include)) {
                    exception.printStackTrace();
                    retryCount++;
                } else {
                    throw exception;
                }
            }
        }

// Recover
Object target = joinPoint.getTarget();
Method recoverMethod = findRecoverMethod(target);
if (recoverMethod != null) {
    log.info("Recover start");
    recoverMethod.invoke(target);
}
        return proceed;
    }

    private boolean instanceContains(Exception target, Class<? extends Exception>[] compareWiths) {
        for (Class<? extends Exception> ex : compareWiths) {
            if (target.getClass().isAssignableFrom(ex)) {
                return true;
            }
        }
        return false;
    }

    public Method findRecoverMethod(Object target) {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(target.getClass());

        // Find @Recover annotation
        for (Method method : methods) {
            if (method.isAnnotationPresent(Recover.class)) {
                // Choose the first recover method
                return method;
            }
        }
        return null;
    }
}
