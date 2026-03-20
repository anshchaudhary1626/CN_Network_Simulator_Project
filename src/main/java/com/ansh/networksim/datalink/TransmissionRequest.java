package com.ansh.networksim.datalink;

import com.ansh.networksim.model.Connection;
import com.ansh.networksim.model.Device;

public class TransmissionRequest {
    private final Device sender;
    private final Connection connection;
    private final Frame frame;

    public TransmissionRequest(Device sender, Connection connection, Frame frame) {
        this.sender = sender;
        this.connection = connection;
        this.frame = frame;
    }

    public Device getSender() {
        return sender;
    }

    public Connection getConnection() {
        return connection;
    }

    public Frame getFrame() {
        return frame;
    }
}