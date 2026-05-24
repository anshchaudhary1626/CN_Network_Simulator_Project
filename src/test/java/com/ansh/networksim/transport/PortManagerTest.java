package com.ansh.networksim.transport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortManagerTest {
    @Test
    void assignsWellKnownAndEphemeralPorts() {
        PortManager manager = new PortManager();

        assertEquals(53, manager.assignWellKnownPort("dns", PortManager.DNS_PORT));
        int ephemeral = manager.assignEphemeralPort("client");

        assertTrue(ephemeral >= PortManager.FIRST_EPHEMERAL_PORT);
        assertEquals(ephemeral, manager.getPort("client"));
    }
}
