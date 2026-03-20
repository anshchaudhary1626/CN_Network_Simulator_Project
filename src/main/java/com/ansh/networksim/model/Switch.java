package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;

import java.util.HashMap;
import java.util.Map;


public class Switch extends Device{
    private final Map<String, Connection> macTable;

    public Switch(int id, String name){
        super(id, name);
        macTable = new HashMap<>();
    }
    @Override
    public void receive(DataPacket packet, Connection fromConnection){
        System.out.println(getName() + " received a physical-layer packet and is ignoring it at L2.");
    }

    @Override
    public void receiveFrame(Frame frame, Connection fromConnection){
        macTable.put(frame.getSourceMac(), fromConnection);
        System.out.println(getName() + " learned source MAC " + frame.getSourceMac() + " on incoming connection.");

        if(Frame.BROADCAST_MAC.equals(frame.getDestinationMac())){
            System.out.println(getName() + " detected a broadcast destination and is forwarding the frame on all other connections.");
            broadcast(frame, fromConnection);
            return;
        }

        Connection target = macTable.get(frame.getDestinationMac());
        if(target != null && target != fromConnection){
            System.out.println(getName() + " found destination MAC " + frame.getDestinationMac() + " in the MAC table.");
            System.out.println(getName() + " forwarding frame to " + frame.getDestinationMac());
            target.transmitFrame(this, frame);
        }
        else{
            System.out.println(getName() + " flooding frame (unknown destination).");
            System.out.println(getName() + " is flooding the frame on all other connections.");
            broadcast(frame, fromConnection);
        }
    }

    public void printMacTable() {
        System.out.println("\n--- MAC Table of " + getName() + " ---");

        if (macTable.isEmpty()) {
            System.out.println("No MAC addresses learned yet.");
            return;
        }

        for (Map.Entry<String, Connection> entry : macTable.entrySet()) {
            System.out.println("MAC: " + entry.getKey() + " -> " + entry.getValue());
        }
    }

    private void broadcast(Frame frame, Connection fromConnection){
        for(Connection connection: getConnections()){
            if(connection != fromConnection){
                connection.transmitFrame(this, frame);
            }
        }
    }
}