package com.ansh.networksim.application;

import com.ansh.networksim.transport.PortManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small DNS-like service.
 *
 * Real DNS converts a domain name into an IP address.
 * Here we keep a simple map:
 * - key: domain name, like www.college.local
 * - value: IP address, like 192.168.2.10
 */
public class DNSService implements ApplicationService {
    private final Map<String, String> records = new LinkedHashMap<>();

    public DNSService() {
        // Built-in DNS record used by the full-stack demo.
        records.put("www.college.local", "192.168.2.10");
    }

    public void addRecord(String domainName, String ipAddress) {
        // Allows tests or future demos to add more fake DNS names.
        records.put(domainName, ipAddress);
    }

    @Override
    public String getServiceName() {
        return "DNS";
    }

    @Override
    public int getPort() {
        return PortManager.DNS_PORT;
    }

    @Override
    public String handleRequest(String request) {
        // Look up the domain name. If not found, return NXDOMAIN like DNS failure.
        System.out.println("[APP-DNS] Query: " + request);
        return records.getOrDefault(request, "NXDOMAIN");
    }
}
