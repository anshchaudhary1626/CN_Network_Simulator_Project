package com.ansh.networksim.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutingTableTest {
    @Test
    void lookupUsesLongestPrefixMatch() {
        RoutingTable table = new RoutingTable();
        table.addRoute(new RouteEntry(new IPv4Address("192.168.0.0/16"), "10.0.0.1", "s0/0", 1, RouteType.STATIC));
        table.addRoute(new RouteEntry(new IPv4Address("192.168.2.0/24"), "10.0.0.2", "s0/1", 1, RouteType.STATIC));

        RouteEntry match = table.lookup("192.168.2.10").orElseThrow();

        assertEquals("s0/1", match.getOutgoingInterface());
        assertEquals(24, match.getPrefixLength());
    }

    @Test
    void staticRouteLookupReturnsConfiguredRoute() {
        Router router = new Router("R1");
        router.addInterface("g0/0", new IPv4Address("10.0.0.1/24"), "00:01");
        router.addStaticRoute("172.16.1.0/24", "10.0.0.2", "g0/0", 1);

        RouteEntry match = router.getRoutingTable().lookup("172.16.1.44").orElseThrow();

        assertEquals(RouteType.STATIC, match.getRouteType());
        assertEquals("10.0.0.2", match.getNextHop());
    }

    @Test
    void ripUpdateLearnsAdvertisedNetwork() {
        Router r1 = new Router("R1");
        r1.addInterface("g0/0", new IPv4Address("192.168.1.1/24"), "00:01");
        Router r2 = new Router("R2");
        r2.addInterface("s0/0", new IPv4Address("10.0.0.2/30"), "00:02");

        new RIPProtocol().exchangeRoutes(r1, r2, "10.0.0.1", "s0/0");

        RouteEntry learned = r2.getRoutingTable().lookup("192.168.1.50").orElseThrow();
        assertEquals(RouteType.RIP, learned.getRouteType());
        assertEquals(1, learned.getMetric());
        assertTrue(learned.matches("192.168.1.50"));
    }
}
