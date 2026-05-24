package com.ansh.networksim.network;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Stores all routes known by one router.
 *
 * In simple words:
 * - A router reads the destination IP of a packet.
 * - It searches this table to decide where to forward the packet.
 * - If multiple routes match, it chooses the most specific one.
 *
 * That "most specific route wins" rule is Longest Prefix Match.
 */
public class RoutingTable {
    private final List<RouteEntry> entries = new ArrayList<>();

    public void addRoute(RouteEntry entry) {
        // Replace the older route of the same type to the same network, then add the new one.
        removeRoute(entry.getDestinationNetwork(), entry.getRouteType());
        entries.add(entry);
    }

    public boolean addOrReplaceIfBetter(RouteEntry candidate) {
        // RIP should update the table only when it finds a cheaper route to the same network.
        Optional<RouteEntry> existing = entries.stream()
                .filter(entry -> entry.getDestinationNetwork().equals(candidate.getDestinationNetwork()))
                .min(Comparator.comparingInt(RouteEntry::getMetric));
        if (existing.isEmpty() || candidate.getMetric() < existing.get().getMetric()) {
            // Remove old RIP route to that network, but keep static/connected entries visible for the demo.
            removeRoute(candidate.getDestinationNetwork(), candidate.getRouteType());
            entries.removeIf(entry -> entry.getDestinationNetwork().equals(candidate.getDestinationNetwork())
                    && entry.getRouteType() == RouteType.RIP);
            entries.add(candidate);
            return true;
        }
        return false;
    }

    public void removeRoute(IPv4Address destinationNetwork, RouteType routeType) {
        // Remove only routes matching both destination and type.
        Iterator<RouteEntry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            RouteEntry entry = iterator.next();
            if (entry.getDestinationNetwork().equals(destinationNetwork.networkPrefix()) && entry.getRouteType() == routeType) {
                iterator.remove();
            }
        }
    }

    public Optional<RouteEntry> lookup(String destinationIp) {
        // Find all matching routes, then choose the longest prefix. If tied, lower metric is better.
        return entries.stream()
                .filter(entry -> entry.matches(destinationIp))
                .max(Comparator.comparingInt(RouteEntry::getPrefixLength)
                        .thenComparing(route -> -route.getMetric()));
    }

    public List<RouteEntry> getEntries() {
        return List.copyOf(entries);
    }

    public void print(String routerName) {
        // Print routes in a stable order so output is easy to read.
        System.out.println("[ROUTER] Routing table for " + routerName);
        entries.stream()
                .sorted(Comparator.comparing(RouteEntry::getDestinationNetwork, Comparator.comparing(IPv4Address::toString)))
                .forEach(entry -> System.out.println("[ROUTER]   " + entry));
    }
}
