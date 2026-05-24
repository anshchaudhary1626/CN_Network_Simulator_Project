package com.ansh.networksim.datalink;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks which transmissions currently occupy the shared medium and what tick the demo is on.
 */
public class SharedMedium {
    private final String mediumName;
    private int currentTick;
    private boolean busy;
    private List<TransmissionRequest> activeRequests;

    // Create a medium with a default descriptive name.
    public SharedMedium() {
        this("shared medium");
    }

    // Create a medium with a custom name for printed traces.
    public SharedMedium(String mediumName) {
        this.mediumName = mediumName;
        this.activeRequests = List.of();
    }

    // Report whether the medium is currently free.
    public boolean isIdle() {
        return !busy;
    }

    // Expose the label used in collision traces.
    public String getMediumName() {
        return mediumName;
    }

    // Expose the current simulated tick.
    public int getCurrentTick() {
        return currentTick;
    }

    // Mark a new set of requests as active on the medium.
    public void beginTransmissionRound(List<TransmissionRequest> requests) {
        activeRequests = new ArrayList<>(requests);
        busy = !activeRequests.isEmpty();
    }

    // Detect whether more than one sender is using the medium at once.
    public boolean hasCollision() {
        return activeRequests.size() > 1;
    }

    // Convenience helper that starts a round and immediately reports whether it collides.
    public boolean hasCollision(List<TransmissionRequest> requests) {
        beginTransmissionRound(requests);
        return hasCollision();
    }

    // Return a safe copy of the active contenders.
    public List<TransmissionRequest> getActiveRequests() {
        return List.copyOf(activeRequests);
    }

    // Move the simulation clock ahead by one tick.
    public void advanceTick() {
        currentTick++;
    }

    // Move the simulation clock ahead by several ticks.
    public void advanceTicks(int count) {
        currentTick += count;
    }

    // Clear all occupancy state after a transmission round ends.
    public void clear() {
        activeRequests = List.of();
        busy = false;
    }
}
