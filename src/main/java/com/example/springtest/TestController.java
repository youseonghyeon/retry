package com.example.springtest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/")
    @Retryable(maxAttempts = 2, include = IllegalArgumentException.class, backoff = 1000)
    public String exTest() {
        throw new IllegalArgumentException("");
    }

    @Recover
    public void recoverTest() {
        log.info("Recover Success");
    }
}
