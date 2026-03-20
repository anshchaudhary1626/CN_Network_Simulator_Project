package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;

public class Connection {
    private final Device device1;
    private final Device device2;

    public Connection(Device device1, Device device2){
        this.device1 = device1;
        this.device2 = device2;
    }

    public Device getDevice1() {
        return device1;
    }

    public Device getDevice2() {
        return device2;
    }

    public Device getOtherDevice(Device current){
        if(current == device1) return device2;
        else if(current == device2) return device1;

        throw new IllegalArgumentException("Device is not part of this Connection");
    }

    public void transmit(Device sender, DataPacket packet){
        Device reciever = getOtherDevice(sender);
        System.out.println("Physical-layer packet transmitted from " + sender.getName() + " to " + reciever.getName() + ".");
        reciever.receive(packet, this);
    }

    public void transmitFrame(Device sender, Frame frame){
        Device reciever = getOtherDevice(sender);
        System.out.println("Data-link frame transmitted from " + sender.getName() + " to " + reciever.getName() + ".");
        reciever.receiveFrame(frame, this);
    }
    @Override
    public String toString() {
        return "Connection{" +
                "device1=" + device1.getName() +
                ", device2=" + device2.getName() +
                '}';
    }
}