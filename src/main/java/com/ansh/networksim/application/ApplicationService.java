package com.ansh.networksim.application;

/**
 * Common contract for any application-layer service in this simulator.
 *
 * In simple words:
 * - DNSService and HTTPService are different services.
 * - But both have a name, a port, and a way to handle a request.
 * - This interface lets the simulator treat them in a common way.
 */
public interface ApplicationService {
    // Human-readable service name, for example "DNS" or "HTTP".
    String getServiceName();

    // Transport-layer port where this service listens.
    int getPort();

    // Take a request string and return the service response string.
    String handleRequest(String request);
}
