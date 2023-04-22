package com.example.springtest;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retryable {
    @Min(1)
    @Max(50)
    int maxAttempts() default 3;

    Class<? extends Exception>[] include() default {Exception.class};

    Class<? extends Exception>[] exclude() default {};

    @Min(0)
    @Max(600000)
    int backoff() default 0;

    boolean printStackTrace() default false;
}
