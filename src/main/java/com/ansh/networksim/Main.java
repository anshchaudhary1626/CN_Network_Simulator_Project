package com.ansh.networksim;

import com.ansh.networksim.simulation.FullStackSimulation;

/**
 * This is the only class Java starts when we run the project.
 *
 * In simple words:
 * - Main is like the "power button" of the simulator.
 * - It does not contain networking logic.
 * - It only creates FullStackSimulation and tells it to run.
 *
 * We keep Main small so the project does not become a messy menu program.
 */
public class Main {
    public static void main(String[] args) {
        // Start the complete automatic demo: application -> transport -> network -> data link -> physical.
        FullStackSimulation simulation = new FullStackSimulation();
        simulation.run();
    }
}
