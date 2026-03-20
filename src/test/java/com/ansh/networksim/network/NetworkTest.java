package com.ansh.networksim.network;

import com.ansh.networksim.model.EndDevice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NetworkTest {

    @Test
    void addDeviceRejectsDuplicateNames() {
        Network network = new Network();

        network.addDevice(new EndDevice(1, "A"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> network.addDevice(new EndDevice(2, "A"))
        );

        assertEquals("Device with name 'A' already exists", exception.getMessage());
    }

    @Test
    void getDeviceRejectsBlankNames() {
        Network network = new Network();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> network.getDevice("   ")
        );

        assertEquals("Device name cannot be blank", exception.getMessage());
    }

    @Test
    void connectRejectsSelfConnection() {
        Network network = new Network();
        network.addDevice(new EndDevice(1, "A"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> network.connect("A", "A")
        );

        assertEquals("A device cannot be connected to itself: A", exception.getMessage());
    }

    @Test
    void connectRejectsDuplicateConnections() {
        Network network = new Network();
        network.addDevice(new EndDevice(1, "A"));
        network.addDevice(new EndDevice(2, "B"));

        network.connect("A", "B");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> network.connect("B", "A")
        );

        assertEquals("Connection already exists", exception.getMessage());
    }

    @Test
    void validConnectionCanBeCreated() {
        Network network = new Network();
        EndDevice a = new EndDevice(1, "A");
        EndDevice b = new EndDevice(2, "B");

        network.addDevice(a);
        network.addDevice(b);

        assertDoesNotThrow(() -> network.connect("A", "B"));
        assertEquals(1, network.countBroadcastDomains());
        assertEquals(1, a.getConnections().size());
        assertEquals(1, b.getConnections().size());
    }
}
