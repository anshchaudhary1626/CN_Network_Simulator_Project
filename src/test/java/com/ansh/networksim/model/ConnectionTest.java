package com.ansh.networksim.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks the basic endpoint lookup behavior of a point-to-point connection.
 */
class ConnectionTest {

    // Verify endpoint lookup returns the device at the other end of the link.
    @Test
    void getOtherDeviceReturnsOppositeEndpoint() {
        EndDevice a = new EndDevice(1, "A");
        EndDevice b = new EndDevice(2, "B");
        Connection connection = new Connection(a, b);

        assertEquals(b, connection.getOtherDevice(a));
        assertEquals(a, connection.getOtherDevice(b));
    }

    // Verify unrelated devices cannot query a connection they do not belong to.
    @Test
    void getOtherDeviceRejectsUnrelatedDevice() {
        EndDevice a = new EndDevice(1, "A");
        EndDevice b = new EndDevice(2, "B");
        EndDevice c = new EndDevice(3, "C");
        Connection connection = new Connection(a, b);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> connection.getOtherDevice(c)
        );

        assertEquals("Device is not part of this Connection", exception.getMessage());
    }
}
