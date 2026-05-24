package com.ansh.networksim.application;

/**
 * Represents the client-side application behavior.
 *
 * In simple words:
 * - A browser first asks DNS: "What IP address is this website?"
 * - Then it sends HTTP: "Give me this web page."
 *
 * This class creates those request messages for the demo.
 */
public class ApplicationClient {
    public String createDnsQuery(String domainName) {
        // Build the message that will be sent to the DNS service.
        System.out.println("[APPLICATION] Client sends DNS query for " + domainName);
        return domainName;
    }

    public String createHttpGet(String path) {
        // Build a very small HTTP-like GET request.
        String request = "GET " + path;
        System.out.println("[APPLICATION] Client sends HTTP request: " + request);
        return request;
    }
}
