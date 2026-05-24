package com.ansh.networksim.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationServiceTest {
    @Test
    void dnsServiceReturnsConfiguredIp() {
        DNSService service = new DNSService();

        assertEquals("192.168.2.10", service.handleRequest("www.college.local"));
    }

    @Test
    void httpServiceReturnsIndexResponse() {
        HTTPService service = new HTTPService();

        String response = service.handleRequest("GET /index.html");

        assertTrue(response.contains("200 OK"));
        assertTrue(response.contains("Welcome to College Network Lab"));
    }
}
