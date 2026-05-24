package com.ansh.networksim.datalink;

import com.ansh.networksim.model.Connection;
import com.ansh.networksim.model.Device;

/**
 * Bundles the sender, link, and frame involved in one transmission attempt.
 */
public class TransmissionRequest {
    private final Device sender;
    private final Connection connection;
    private final Frame frame;

    // Store the sender, its outgoing link, and the frame being attempted.
    public TransmissionRequest(Device sender, Connection connection, Frame frame) {
        this.sender = sender;
        this.connection = connection;
        this.frame = frame;
    }

    // Return the device initiating the transmission.
    public Device getSender() {
        return sender;
    }

    // Return the link on which the frame will be sent.
    public Connection getConnection() {
        return connection;
    }

    // Return the frame associated with this attempt.
    public Frame getFrame() {
        return frame;
    }
}
