package com.ansh.networksim.network;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a router.
 *
 * In simple words:
 * - A router has multiple interfaces.
 * - Each interface belongs to a network.
 * - The router keeps a routing table.
 * - When a packet arrives, the router uses Longest Prefix Match to choose the best route.
 */
public class Router {
    private final String name;
    private final Map<String, RouterInterface> interfaces = new LinkedHashMap<>();
    private final RoutingTable routingTable = new RoutingTable();

    public Router(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public RouterInterface addInterface(String interfaceName, IPv4Address ipAddress, String macAddress) {
        // Adding an interface also means the router is directly connected to that interface's network.
        RouterInterface routerInterface = new RouterInterface(interfaceName, ipAddress, macAddress);
        interfaces.put(interfaceName, routerInterface);
        routingTable.addRoute(new RouteEntry(ipAddress.networkPrefix(), null, interfaceName, 0, RouteType.CONNECTED));
        return routerInterface;
    }

    public void addStaticRoute(String destinationCidr, String nextHop, String outgoingInterface, int metric) {
        // Static route means the network administrator manually configured this path.
        RouteEntry route = new RouteEntry(new IPv4Address(destinationCidr), nextHop, outgoingInterface, metric, RouteType.STATIC);
        routingTable.addRoute(route);
        System.out.println("[ROUTER] " + name + " installed static route: " + route);
    }

    public boolean learnRipRoute(RouteEntry route) {
        // RIP route means this path was learned from another router's update.
        boolean changed = routingTable.addOrReplaceIfBetter(route);
        if (changed) {
            System.out.println("[RIP] " + name + " learned route: " + route);
        }
        return changed;
    }

    public Optional<RouteEntry> forward(IPv4Packet packet) {
        // This simulates the router reading the destination IP and choosing an outgoing route.
        System.out.println("[ROUTER] " + name + " performs longest prefix match for destination " + packet.getDestinationIp());
        Optional<RouteEntry> route = routingTable.lookup(packet.getDestinationIp());
        route.ifPresent(entry -> System.out.println("[ROUTER] Selected route: " + entry));
        return route;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public Map<String, RouterInterface> getInterfaces() {
        return Map.copyOf(interfaces);
    }

    public RouterInterface getInterface(String interfaceName) {
        // Fail fast if the simulation refers to an interface that does not exist.
        RouterInterface routerInterface = interfaces.get(interfaceName);
        if (routerInterface == null) {
            throw new IllegalArgumentException("Interface not found on " + name + ": " + interfaceName);
        }
        return routerInterface;
    }
}
