package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;
public class Hub extends Device{
    public Hub(int id, String name){
        super(id, name);
    }

    @Override
    public void receive(DataPacket packet, Connection fromConnection){
        System.out.println(getName() + " received a physical-layer packet and is broadcasting it to all other connections.");

        for(Connection connection : getConnections()){
            if(connection != fromConnection){
                connection.transmit(this, packet);
            }
        }
    }

    @Override
    public void receiveFrame(Frame frame, Connection fromConnection) {
        System.out.println(getName() + " received a data-link frame and is broadcasting it to all other connections.");

        for (Connection connection : getConnections()) {
            if (connection != fromConnection) {
                connection.transmitFrame(this, frame);
            }
        }
    }
}