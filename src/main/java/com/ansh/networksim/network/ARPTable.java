package com.ansh.networksim.network;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stores IP address to MAC address mappings.
 *
 * In simple words:
 * - IP address is used by the network layer.
 * - MAC address is needed to actually send a frame on the local link.
 * - ARP is the question: "Who has this IP? Tell me your MAC."
 *
 * ARP only works inside the same local network.
 */
public class ARPTable {
    private final String ownerName;
    private final Map<String, String> mappings = new LinkedHashMap<>();

    public ARPTable(String ownerName) {
        this.ownerName = ownerName;
    }

    public void addEntry(String ipAddress, String macAddress) {
        // Manually add a known IP -> MAC mapping.
        mappings.put(ipAddress, macAddress);
    }

    public Optional<String> lookup(String ipAddress) {
        // Check if the MAC address is already cached.
        return Optional.ofNullable(mappings.get(ipAddress));
    }

    public String resolve(IPv4Address requesterIp, String targetIp, String requesterMac, Map<String, String> sameNetworkHosts) {
        // ARP cannot find devices across routers. It only resolves local neighbors.
        if (!requesterIp.isSameNetwork(new IPv4Address(targetIp + "/" + requesterIp.getPrefixLength()))) {
            throw new IllegalArgumentException("ARP can only resolve hosts in the same local network");
        }

        String cached = mappings.get(targetIp);
        if (cached != null) {
            // If we already know the MAC, no broadcast is needed.
            System.out.println("[ARP] " + ownerName + " cache hit: " + targetIp + " -> " + cached);
            return cached;
        }

        // Simulate ARP broadcast and reply using the provided local-network host map.
        System.out.println("[ARP] " + ownerName + " broadcasts ARP request for " + targetIp + " from MAC " + requesterMac);
        String resolvedMac = sameNetworkHosts.get(targetIp);
        if (resolvedMac == null) {
            throw new IllegalArgumentException("No ARP reply for " + targetIp);
        }
        System.out.println("[ARP] " + targetIp + " replies with MAC " + resolvedMac);
        // Store the result so next time it becomes a cache hit.
        mappings.put(targetIp, resolvedMac);
        return resolvedMac;
    }
}
