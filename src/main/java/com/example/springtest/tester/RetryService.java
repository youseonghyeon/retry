package com.example.springtest.tester;

import com.example.springtest.Recover;
import com.example.springtest.Retryable;
import org.springframework.stereotype.Service;

@Service
public class RetryService {

    @Retryable(include = Exception.class, exclude = NullPointerException.class)
    public String excludeNullPoint() {
        throw new NullPointerException("임의 생성 예외");
    }

    @Recover
    public String recoverMethod() {
        return "success";
    }

    @Retryable(include = Exception.class, exclude = NullPointerException.class)
    public String illegalState() {
        throw new IllegalStateException("임의 생성 예외");
    }
}
