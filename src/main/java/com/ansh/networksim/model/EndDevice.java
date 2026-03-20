package com.ansh.networksim.model;


import com.ansh.networksim.datalink.Frame;
import com.ansh.networksim.datalink.FrameType;

public class EndDevice extends Device{
    public EndDevice(int id, String name) {
        super(id, name);
    }

    public void send(String destination, String message){
        if(getConnections().isEmpty()){
            throw new IllegalStateException(getName() + " is not connected to any device");
        }

        DataPacket packet = new DataPacket(getName(), destination, message);
        System.out.println(getName() + " is sending a physical-layer packet to " + destination + " with payload: " + message);

        getConnections().get(0).transmit(this, packet);
    }

    @Override
    public void receive(DataPacket packet, Connection fromConnection){
        if(getName().equals(packet.getDestination())){
            System.out.println(getName() + " received the physical-layer message: " + packet.getPayload());
        }
        else {
            System.out.println(getName() + " received a physical-layer packet not meant for it and ignored it.");
        }
    }

    public void sendFrame(String destinationMac, String message) {
        if (getConnections().isEmpty()) {
            throw new IllegalStateException(getName() + " is not connected to any device");
        }

        Frame frame = Frame.createDataFrame(getName(), destinationMac, 0, message);
        System.out.println(getName() + " is sending a data-link frame to " + destinationMac + " with payload: " + message);

        getConnections().get(0).transmitFrame(this, frame);
    }

    @Override
    public void receiveFrame(Frame frame, Connection fromConnection) {
        if (frame.getType() == FrameType.ACK) {
            System.out.println(getName() + " received ACK for sequence number " + frame.getSequenceNumber() + ".");
            return;
        }

        if (getName().equals(frame.getDestinationMac()) || Frame.BROADCAST_MAC.equals(frame.getDestinationMac())) {
            System.out.println(getName() + " received the frame payload: " + frame.getPayload());
        } else {
            System.out.println(getName() + " received a frame not meant for it and ignored it.");
        }
    }
}
