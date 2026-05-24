package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;

import java.util.HashMap;
import java.util.Map;


/**
 * Learning switch that maps source MAC addresses to incoming connections.
 */
public class Switch extends Device{
    private final Map<String, Connection> macTable;

    // Create a switch with an empty MAC learning table.
    public Switch(int id, String name){
        super(id, name);
        macTable = new HashMap<>();
    }

    // Switches ignore physical packets because this class models layer-2 behavior.
    @Override
    public void receive(DataPacket packet, Connection fromConnection){
        System.out.println(getName() + " received a physical-layer packet and is ignoring it at L2.");
    }

    // Learn the source MAC and forward known destinations intelligently.
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

    // Print the current switch MAC table for the demo.
    public void printMacTable() {
        System.out.println("\n--- MAC Table of " + getName() + " ---");

        if (macTable.isEmpty()) {
            System.out.println("No MAC addresses learned yet.");
            return;
        }

        // Print each learned MAC-to-port mapping.
        for (Map.Entry<String, Connection> entry : macTable.entrySet()) {
            System.out.println("MAC: " + entry.getKey() + " -> " + entry.getValue());
        }
    }

    // Unknown destinations are flooded so the frame still has a chance to reach the target.
    private void broadcast(Frame frame, Connection fromConnection){
        // Send the frame out of every port except the incoming port.
        for(Connection connection: getConnections()){
            if(connection != fromConnection){
                connection.transmitFrame(this, frame);
            }
        }
    }
}