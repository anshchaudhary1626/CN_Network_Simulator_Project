package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;

import java.util.HashMap;
import java.util.Map;

/**
 * Two-port learning bridge that forwards frames using a small MAC table.
 */
public class Bridge extends Device{
    private static final int MAX_PORTS = 2;
    private final Map<String, Connection> macTable;

    // Initialize the bridge and its empty MAC learning table.
    public Bridge(int id, String name){
        super(id, name);
        macTable = new HashMap<>();
    }

    // Enforce the two-port limit that distinguishes a bridge from a larger switch.
    @Override
    public void addConnection(Connection connection) {
        if(getConnections().size() >= MAX_PORTS){
            throw new IllegalStateException(getName() + " only supports " + MAX_PORTS + " ports ");
        }
        else{
            super.addConnection(connection);
        }
    }

    // Bridges ignore physical-layer packets in this layer-2-focused simulation.
    @Override
    public void receive(DataPacket dataPacket, Connection fromConnection){
        System.out.println(getName() + " received a physical layer packet and is ignoring it at L2.");
    }

    // Learn the source MAC and either unicast forward or flood the frame.
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
            System.out.println(getName() + " did not find destination MAC " + frame.getDestinationMac() + " in the MAC table.");
            System.out.println(getName() + " flooding frame (unknown destination).");
            broadcast(frame, fromConnection);
        }
    }

    // Print the current bridge MAC table for demo purposes.
    public void printMacTable() {
        System.out.println("\n--- MAC Table of " + getName() + " ---");

        if (macTable.isEmpty()) {
            System.out.println("No MAC addresses learned yet.");
            return;
        }

        // Print each learned MAC-to-port mapping on its own line.
        for (Map.Entry<String, Connection> entry : macTable.entrySet()) {
            System.out.println("MAC: " + entry.getKey() + " -> " + entry.getValue());
        }
    }

    // Flooding is used for broadcasts and for destinations that have not been learned yet.
    private void broadcast(Frame frame, Connection fromConnection){
        // Send the frame on every port except the one it arrived on.
        for(Connection connection: getConnections()){
            if(connection != fromConnection){
                connection.transmitFrame(this, frame);
            }
        }
    }
}
