package com.ansh.networksim.model;


import com.ansh.networksim.datalink.Frame;
import com.ansh.networksim.datalink.FrameType;
import com.ansh.networksim.simulation.PayloadUtil;

/**
 * Host node that originates and consumes packets/frames.
 */
public class EndDevice extends Device{
    // Create an end device that can originate and receive traffic.
    public EndDevice(int id, String name) {
        super(id, name);
    }

    // Physical sends always leave through the first connected link in this simplified model.
    public void send(String destination, String message){
        if(getConnections().isEmpty()){
            throw new IllegalStateException(getName() + " is not connected to any device");
        }

        DataPacket packet = new DataPacket(getName(), destination, message);
        System.out.println(getName() + " is sending a physical-layer packet to " + destination
                + " with payload: " + PayloadUtil.display(message));

        getConnections().get(0).transmit(this, packet);
    }

    // Accept only packets whose destination matches this device.
    @Override
    public void receive(DataPacket packet, Connection fromConnection){
        if(getName().equals(packet.getDestination())){
            System.out.println(getName() + " received the physical-layer message: " + PayloadUtil.display(packet.getPayload()));
        }
        else {
            System.out.println(getName() + " received a physical-layer packet not meant for it and ignored it.");
        }
    }

    // Data-link sends build a frame with checksum and MAC-style addressing.
    public void sendFrame(String destinationMac, String message) {
        if (getConnections().isEmpty()) {
            throw new IllegalStateException(getName() + " is not connected to any device");
        }

        Frame frame = Frame.createDataFrame(getName(), destinationMac, 0, message);
        System.out.println(getName() + " is sending a data-link frame to " + destinationMac
                + " with payload: " + PayloadUtil.display(message));

        getConnections().get(0).transmitFrame(this, frame);
    }

    // Accept ACKs specially and accept DATA only when addressed to this host or broadcast.
    @Override
    public void receiveFrame(Frame frame, Connection fromConnection) {
        if (frame.getType() == FrameType.ACK) {
            System.out.println(getName() + " received ACK for sequence number " + frame.getSequenceNumber() + ".");
            return;
        }

        if (getName().equals(frame.getDestinationMac()) || Frame.BROADCAST_MAC.equals(frame.getDestinationMac())) {
            System.out.println(getName() + " received the frame payload: " + PayloadUtil.display(frame.getPayload()));
        } else {
            System.out.println(getName() + " received a frame not meant for it and ignored it.");
        }
    }
}
