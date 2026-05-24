package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;

/**
 * Physical repeater that blindly broadcasts incoming traffic on all other ports.
 */
public class Hub extends Device{
    // Create a hub that blindly repeats incoming traffic.
    public Hub(int id, String name){
        super(id, name);
    }

    // Broadcast every physical-layer packet to all ports except the incoming one.
    @Override
    public void receive(DataPacket packet, Connection fromConnection){
        System.out.println(getName() + " received a physical-layer packet and is broadcasting it to all other connections.");

        // Repeat the packet on every other attached connection.
        for(Connection connection : getConnections()){
            if(connection != fromConnection){
                connection.transmit(this, packet);
            }
        }
    }

    // Broadcast every data-link frame to all ports except the incoming one.
    @Override
    public void receiveFrame(Frame frame, Connection fromConnection) {
        System.out.println(getName() + " received a data-link frame and is broadcasting it to all other connections.");

        // Repeat the frame on every other attached connection.
        for (Connection connection : getConnections()) {
            if (connection != fromConnection) {
                connection.transmitFrame(this, frame);
            }
        }
    }
}
