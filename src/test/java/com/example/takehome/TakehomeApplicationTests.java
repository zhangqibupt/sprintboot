package com.example.takehome;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.endpoint.http.ApiVersion;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TakehomeApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(ApiVersion.LATEST.equals(ApiVersion.V3));
    }
}
