package com.example.springtest;

import com.example.springtest.tester.RetryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RetryableAspectTest {

    @Autowired
    RetryService retryService;

    @Test
    void nullPointTest() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            retryService.excludeNullPoint();
        });
    }

    @Test
    void IllegalStateExTest() {
        Assertions.assertEquals("success", retryService.illegalState());
    }

}
