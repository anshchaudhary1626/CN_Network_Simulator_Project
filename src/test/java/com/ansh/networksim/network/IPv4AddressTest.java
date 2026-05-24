package com.ansh.networksim.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IPv4AddressTest {
    @Test
    void validatesCidrNotation() {
        IPv4Address address = new IPv4Address("192.168.1.10/24");

        assertEquals("192.168.1.10", address.getAddress());
        assertEquals(24, address.getPrefixLength());
        assertEquals("255.255.255.0", address.getSubnetMask());
        assertThrows(IllegalArgumentException.class, () -> new IPv4Address("192.168.1.300/24"));
        assertThrows(IllegalArgumentException.class, () -> new IPv4Address("192.168.1.10"));
    }

    @Test
    void checksSameNetwork() {
        IPv4Address host = new IPv4Address("192.168.1.10/24");
        IPv4Address gateway = new IPv4Address("192.168.1.1/24");

        assertTrue(host.isSameNetwork(gateway));
        assertEquals("192.168.1.0", host.getNetworkAddress());
    }
}
