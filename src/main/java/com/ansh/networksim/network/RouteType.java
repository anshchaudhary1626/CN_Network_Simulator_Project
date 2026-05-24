package com.ansh.networksim.network;

/**
 * Tells how a route entered the routing table.
 *
 * CONNECTED: network is directly attached to the router.
 * STATIC: route was manually configured.
 * RIP: route was learned from RIP dynamic routing.
 */
public enum RouteType {
    CONNECTED,
    STATIC,
    RIP
}
