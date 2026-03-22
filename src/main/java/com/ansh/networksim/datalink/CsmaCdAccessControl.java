package com.ansh.networksim.datalink;

import com.ansh.networksim.simulation.TransmissionTraceRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CsmaCdAccessControl {
    private static final int MAX_RETRIES = 3;
    private final Random random;

    public CsmaCdAccessControl() {
        this(new Random(42));
    }

    public CsmaCdAccessControl(Random random) {
        this.random = random;
    }

    public void simulateSlot(SharedMedium medium, List<TransmissionRequest> requests){
        if(requests.isEmpty()) return;

        System.out.println("--- CSMA/CD Slot ---");
        medium.beginTransmissionRound(requests);

        if(!medium.hasCollision()){
            TransmissionRequest request = requests.get(0);
            System.out.println("Tick " + medium.getCurrentTick() + ": " + request.getSender().getName()
                    + " senses an idle medium.");
            System.out.println(request.getSender().getName() + " transmitted successfully without collision.");
            int nextTick = TransmissionTraceRenderer.renderFrameFlow(
                    request.getSender().getName(),
                    request.getConnection().getOtherDevice(request.getSender()).getName(),
                    request.getFrame(),
                    medium.getCurrentTick()
            );
            medium.advanceTicks(nextTick - medium.getCurrentTick());
            request.getConnection().transmitFrame(request.getSender(), request.getFrame());
            medium.clear();
            System.out.println("--- End CSMA/CD Slot ---");
            return;
        }

        for (TransmissionRequest request : requests) {
            System.out.println("Tick " + medium.getCurrentTick() + ": " + request.getSender().getName() + " senses the medium as idle and starts transmitting.");
        }
        System.out.println("Collision detected for " + medium.getActiveRequests().size() + " simultaneous transmissions.");
        int nextTick = TransmissionTraceRenderer.renderCollision(
                medium.getMediumName(),
                medium.getActiveRequests(),
                medium.getCurrentTick()
        );
        medium.advanceTicks(nextTick - medium.getCurrentTick());
        medium.clear();

        List<TransmissionRequest> pendingRequests = new ArrayList<>(requests);
        Map<TransmissionRequest, Integer> retryCounts = new HashMap<>();
        Map<TransmissionRequest, Integer> backoffSlots = new HashMap<>();
        int round = 1;

        scheduleBackoff(pendingRequests, retryCounts, backoffSlots);

        while(!pendingRequests.isEmpty()){
            System.out.println("Backoff round " + round + ":");
            int earliestAttempt = pendingRequests.stream()
                    .mapToInt(request -> backoffSlots.getOrDefault(request, 0))
                    .min()
                    .orElse(0);

            if (earliestAttempt > 0) {
                for (int idleSlot = 0; idleSlot < earliestAttempt; idleSlot++) {
                    System.out.println("Tick " + medium.getCurrentTick() + ": medium idle during backoff slot " + (idleSlot + 1)
                            + "/" + earliestAttempt + ".");
                    medium.advanceTick();
                }
            }

            List<TransmissionRequest> contenders = new ArrayList<>();
            List<TransmissionRequest> survivors = new ArrayList<>();

            for (TransmissionRequest request : pendingRequests) {
                int retryAttempt = retryCounts.getOrDefault(request, 0);
                int remainingBackoff = backoffSlots.getOrDefault(request, 0) - earliestAttempt;
                if (remainingBackoff == 0) {
                    System.out.println("Retry attempt " + retryAttempt + " for " + request.getSender().getName() + ".");
                    contenders.add(request);
                } else {
                    System.out.println(request.getSender().getName() + " still has " + remainingBackoff + " backoff slot(s) remaining.");
                    backoffSlots.put(request, remainingBackoff);
                    survivors.add(request);
                }
            }

            if (contenders.size() == 1) {
                TransmissionRequest winner = contenders.get(0);
                medium.beginTransmissionRound(List.of(winner));
                System.out.println("Tick " + medium.getCurrentTick() + ": " + winner.getSender().getName() + " senses the medium as idle after backoff.");
                System.out.println(winner.getSender().getName() + " successfully transmitted after backoff.");
                nextTick = TransmissionTraceRenderer.renderFrameFlow(
                        winner.getSender().getName(),
                        winner.getConnection().getOtherDevice(winner.getSender()).getName(),
                        winner.getFrame(),
                        medium.getCurrentTick()
                );
                medium.advanceTicks(nextTick - medium.getCurrentTick());
                winner.getConnection().transmitFrame(winner.getSender(), winner.getFrame());
                medium.clear();
                pendingRequests = survivors;
            } else {
                medium.beginTransmissionRound(contenders);
                System.out.println("Collision detected for " + medium.getActiveRequests().size() + " retransmission attempt(s).");
                nextTick = TransmissionTraceRenderer.renderCollision(
                        medium.getMediumName(),
                        medium.getActiveRequests(),
                        medium.getCurrentTick()
                );
                medium.advanceTicks(nextTick - medium.getCurrentTick());
                medium.clear();
                pendingRequests = new ArrayList<>(survivors);
                pendingRequests.addAll(scheduleBackoff(contenders, retryCounts, backoffSlots));
            }
            round++;
        }

        System.out.println("--- End CSMA/CD Slot ---");
    }

    private List<TransmissionRequest> scheduleBackoff(List<TransmissionRequest> collidingRequests,
                                                      Map<TransmissionRequest, Integer> retryCounts,
                                                      Map<TransmissionRequest, Integer> backoffSlots) {
        List<TransmissionRequest> retryable = new ArrayList<>();
        for (TransmissionRequest request : collidingRequests) {
            int retryCount = retryCounts.getOrDefault(request, 0) + 1;
            retryCounts.put(request, retryCount);

            if (retryCount > MAX_RETRIES) {
                System.out.println(request.getSender().getName() + " exceeded the maximum retry limit and failed to transmit.");
                backoffSlots.remove(request);
                continue;
            }

            int backoffRange = 1 << Math.min(retryCount, 10);
            int selectedBackoff = random.nextInt(backoffRange);
            backoffSlots.put(request, selectedBackoff);
            retryable.add(request);
            System.out.println(request.getSender().getName() + " selected backoff slot " + selectedBackoff
                    + " from range [0, " + (backoffRange - 1) + "].");
        }
        return retryable;
    }
}
