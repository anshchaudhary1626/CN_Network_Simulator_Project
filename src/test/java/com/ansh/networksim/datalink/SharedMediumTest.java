package com.ansh.networksim.datalink;

import com.ansh.networksim.model.Connection;
import com.ansh.networksim.model.EndDevice;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedMediumTest {

    @Test
    void hasCollisionReturnsFalseForSingleRequest() {
        EndDevice sender = new EndDevice(1, "S1");
        EndDevice receiver = new EndDevice(2, "S2");
        Connection connection = new Connection(sender, receiver);
        TransmissionRequest request = new TransmissionRequest(
                sender,
                connection,
                Frame.createDataFrame("S1", "S2", 0, "payload")
        );

        SharedMedium medium = new SharedMedium();
        assertFalse(medium.hasCollision(List.of(request)));
    }

    @Test
    void hasCollisionReturnsTrueForMultipleRequests() {
        EndDevice s1 = new EndDevice(1, "S1");
        EndDevice s2 = new EndDevice(2, "S2");
        EndDevice r1 = new EndDevice(3, "R1");
        EndDevice r2 = new EndDevice(4, "R2");

        TransmissionRequest request1 = new TransmissionRequest(
                s1,
                new Connection(s1, r1),
                Frame.createDataFrame("S1", "R1", 0, "one")
        );
        TransmissionRequest request2 = new TransmissionRequest(
                s2,
                new Connection(s2, r2),
                Frame.createDataFrame("S2", "R2", 0, "two")
        );

        SharedMedium medium = new SharedMedium();
        assertTrue(medium.hasCollision(List.of(request1, request2)));
    }
}
