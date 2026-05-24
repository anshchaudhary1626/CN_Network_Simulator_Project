package com.ansh.networksim.network;

import java.util.Objects;

/**
 * Represents one row in a router's routing table.
 *
 * In simple words, each route answers:
 * - Which destination network is this route for?
 * - What next-hop router should I send to?
 * - Which router interface should I use?
 * - What is the cost/metric?
 * - Did this route come from static config, RIP, or a directly connected network?
 */
public class RouteEntry {
    private final IPv4Address destinationNetwork;
    private final String nextHop;
    private final String outgoingInterface;
    private final int metric;
    private final RouteType routeType;

    public RouteEntry(IPv4Address destinationNetwork, String nextHop, String outgoingInterface, int metric, RouteType routeType) {
        // Store destination as the network prefix, not as one particular host IP.
        this.destinationNetwork = destinationNetwork.networkPrefix();
        this.nextHop = nextHop;
        this.outgoingInterface = outgoingInterface;
        this.metric = metric;
        this.routeType = routeType;
    }

    public IPv4Address getDestinationNetwork() {
        return destinationNetwork;
    }

    public int getPrefixLength() {
        return destinationNetwork.getPrefixLength();
    }

    public String getNextHop() {
        return nextHop;
    }

    public String getOutgoingInterface() {
        return outgoingInterface;
    }

    public int getMetric() {
        return metric;
    }

    public RouteType getRouteType() {
        return routeType;
    }

    public boolean matches(String destinationIp) {
        // Used during lookup to check if this route can carry traffic to the destination IP.
        return destinationNetwork.contains(destinationIp);
    }

    public boolean sameDestination(RouteEntry other) {
        // Useful when comparing two routes that point to the same network.
        return destinationNetwork.equals(other.destinationNetwork);
    }

    @Override
    public String toString() {
        return destinationNetwork
                + " via " + (nextHop == null ? "direct" : nextHop)
                + " dev " + outgoingInterface
                + " metric " + metric
                + " " + routeType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RouteEntry other)) {
            return false;
        }
        return metric == other.metric
                && Objects.equals(destinationNetwork, other.destinationNetwork)
                && Objects.equals(nextHop, other.nextHop)
                && Objects.equals(outgoingInterface, other.outgoingInterface)
                && routeType == other.routeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationNetwork, nextHop, outgoingInterface, metric, routeType);
    }
}
