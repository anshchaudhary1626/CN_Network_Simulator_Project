package com.ansh.networksim.network;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ARPTableTest {
    @Test
    void resolvesMacAddressForSameNetworkHost() {
        ARPTable table = new ARPTable("HostA");

        String mac = table.resolve(
                new IPv4Address("192.168.1.10/24"),
                "192.168.1.1",
                "00:AA",
                Map.of("192.168.1.1", "00:R1")
        );

        assertEquals("00:R1", mac);
        assertEquals("00:R1", table.lookup("192.168.1.1").orElseThrow());
    }
}
