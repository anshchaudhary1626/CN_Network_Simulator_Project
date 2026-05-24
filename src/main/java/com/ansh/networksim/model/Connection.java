package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;

/**
 * A point-to-point link between exactly two devices.
 */
public class Connection {
    private final Device device1;
    private final Device device2;

    // Create a bidirectional link between two devices.
    public Connection(Device device1, Device device2){
        this.device1 = device1;
        this.device2 = device2;
    }

    // Return the first endpoint.
    public Device getDevice1() {
        return device1;
    }

    // Return the second endpoint.
    public Device getDevice2() {
        return device2;
    }

    // Every transmission is relayed to the opposite endpoint on the same connection.
    public Device getOtherDevice(Device current){
        if(current == device1) return device2;
        else if(current == device2) return device1;

        throw new IllegalArgumentException("Device is not part of this Connection");
    }

    // Deliver a physical-layer packet to the opposite endpoint.
    public void transmit(Device sender, DataPacket packet){
        Device reciever = getOtherDevice(sender);
        System.out.println("Physical-layer packet transmitted from " + sender.getName() + " to " + reciever.getName() + ".");
        reciever.receive(packet, this);
    }

    // Deliver a data-link frame to the opposite endpoint.
    public void transmitFrame(Device sender, Frame frame){
        Device reciever = getOtherDevice(sender);
        System.out.println("Data-link frame transmitted from " + sender.getName() + " to " + reciever.getName() + ".");
        reciever.receiveFrame(frame, this);
    }

    // Print the link endpoints in a readable form.
    @Override
    public String toString() {
        return "Connection{" +
                "device1=" + device1.getName() +
                ", device2=" + device2.getName() +
                '}';
    }
}
