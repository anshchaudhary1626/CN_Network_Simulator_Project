package com.ansh.networksim.application;

import com.ansh.networksim.transport.PortManager;

/**
 * Small HTTP-like web service.
 *
 * Real HTTP has many headers and rules.
 * For this lab, we only simulate one request:
 * - GET /index.html
 *
 * The goal is to show application data travelling through all layers.
 */
public class HTTPService implements ApplicationService {
    @Override
    public String getServiceName() {
        return "HTTP";
    }

    @Override
    public int getPort() {
        return PortManager.HTTP_PORT;
    }

    @Override
    public String handleRequest(String request) {
        // Return a successful fake web page only for the known demo path.
        System.out.println("[APP-HTTP] " + request);
        if ("GET /index.html".equals(request)) {
            return "HTTP/1.1 200 OK\n<html><body>Welcome to College Network Lab</body></html>";
        }
        // Any other path is treated as missing.
        return "HTTP/1.1 404 Not Found";
    }
}
