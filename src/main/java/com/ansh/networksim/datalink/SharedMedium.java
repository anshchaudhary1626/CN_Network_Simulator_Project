package com.ansh.networksim.datalink;

import java.util.ArrayList;
import java.util.List;

public class SharedMedium {
    private final String mediumName;
    private int currentTick;
    private boolean busy;
    private List<TransmissionRequest> activeRequests;

    public SharedMedium() {
        this("shared medium");
    }

    public SharedMedium(String mediumName) {
        this.mediumName = mediumName;
        this.activeRequests = List.of();
    }

    public boolean isIdle() {
        return !busy;
    }

    public String getMediumName() {
        return mediumName;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void beginTransmissionRound(List<TransmissionRequest> requests) {
        activeRequests = new ArrayList<>(requests);
        busy = !activeRequests.isEmpty();
    }

    public boolean hasCollision() {
        return activeRequests.size() > 1;
    }

    public boolean hasCollision(List<TransmissionRequest> requests) {
        beginTransmissionRound(requests);
        return hasCollision();
    }

    public List<TransmissionRequest> getActiveRequests() {
        return List.copyOf(activeRequests);
    }

    public void advanceTick() {
        currentTick++;
    }

    public void advanceTicks(int count) {
        currentTick += count;
    }

    public void clear() {
        activeRequests = List.of();
        busy = false;
    }
}
