package com.ansh.networksim.network;

/**
 * Simulates RIP dynamic routing.
 *
 * In simple words:
 * - Routers tell neighboring routers about networks they know.
 * - The neighbor adds 1 hop to the metric.
 * - A smaller metric is considered a better path.
 *
 * This is not a full real RIP daemon. It is a clean lab simulation of route exchange.
 */
public class RIPProtocol {
    public void exchangeRoutes(Router sender, Router receiver, String receiverNextHop, String receiverOutgoingInterface) {
        // Sender advertises its known non-static routes to the receiver.
        System.out.println("[RIP] " + sender.getName() + " sends routing update to " + receiver.getName());
        for (RouteEntry advertised : sender.getRoutingTable().getEntries()) {
            // Static routes are manually configured; in this simplified demo we advertise connected/RIP routes.
            if (advertised.getRouteType() == RouteType.STATIC) {
                continue;
            }
            // RIP uses hop count. One more router hop means metric + 1.
            int newMetric = advertised.getMetric() + 1;
            if (newMetric > 15) {
                // RIP treats 16 as unreachable, so anything above 15 is ignored.
                continue;
            }
            // Build the route as it would look from the receiver's point of view.
            RouteEntry learned = new RouteEntry(
                    advertised.getDestinationNetwork(),
                    receiverNextHop,
                    receiverOutgoingInterface,
                    newMetric,
                    RouteType.RIP
            );
            receiver.learnRipRoute(learned);
        }
    }
}
