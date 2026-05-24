package com.ansh.networksim.datalink;

import com.ansh.networksim.simulation.TransmissionTraceRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class shows how CSMA/CD works when many devices share the same medium.
 * If two or more devices send at the same time, a collision happens.
 * Then they send a jam signal, wait for some time, and try again.
 */
public class CsmaCdAccessControl {
    private static final int MAX_RETRIES = 3;
    private final Random random;

    // We use a fixed random seed so the demo gives the same result every time.
    public CsmaCdAccessControl() {
        this(new Random(42));
    }

    // This version lets us pass our own random generator if we want.
    public CsmaCdAccessControl(Random random) {
        this.random = random;
    }

    // This method simulates one CSMA/CD transmission slot from start to finish.
    public void simulateSlot(SharedMedium medium, List<TransmissionRequest> requests){
        // If nobody wants to send anything, there is nothing to simulate.
        if(requests.isEmpty()) return;

        System.out.println("--- CSMA/CD Slot ---");

        // Put all current send requests on the shared medium.
        medium.beginTransmissionRound(requests);

        // If only one sender is trying to send, there is no collision.
        // So that sender can use the medium immediately.
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

        // If we are here, it means many senders started together.
        // So we show that all of them began sending at the same time.
        for (TransmissionRequest request : requests) {
            System.out.println("Tick " + medium.getCurrentTick() + ": " + request.getSender().getName() + " senses the medium as idle and starts transmitting.");
        }
        System.out.println("Collision detected for " + medium.getActiveRequests().size() + " simultaneous transmissions.");

        // Now we show the collision and the jam signal on the medium.
        int nextTick = TransmissionTraceRenderer.renderCollision(
                medium.getMediumName(),
                medium.getActiveRequests(),
                medium.getCurrentTick()
        );
        medium.advanceTicks(nextTick - medium.getCurrentTick());
        medium.clear();

        // These are the senders that still need another chance to transmit.
        List<TransmissionRequest> pendingRequests = new ArrayList<>(requests);

        // This map remembers how many times each sender has already retried.
        Map<TransmissionRequest, Integer> retryCounts = new HashMap<>();

        // This map stores how many backoff slots each sender must wait.
        Map<TransmissionRequest, Integer> backoffSlots = new HashMap<>();
        int round = 1;

        // After the collision, each sender picks a waiting time before retrying.
        scheduleBackoff(pendingRequests, retryCounts, backoffSlots);

        // Keep retrying until every sender either succeeds or gives up.
        while(!pendingRequests.isEmpty()){
            System.out.println("Backoff round " + round + ":");

            // We look for the sender that will become ready first.
            // Everyone else will either wait longer or try together at that same moment.
            int earliestAttempt = pendingRequests.stream()
                    .mapToInt(request -> backoffSlots.getOrDefault(request, 0))
                    .min()
                    .orElse(0);

            if (earliestAttempt > 0) {
                // If the earliest sender still has to wait, the medium stays idle for that time.
                for (int idleSlot = 0; idleSlot < earliestAttempt; idleSlot++) {
                    System.out.println("Tick " + medium.getCurrentTick() + ": medium idle during backoff slot " + (idleSlot + 1)
                            + "/" + earliestAttempt + ".");
                    medium.advanceTick();
                }
            }

            List<TransmissionRequest> contenders = new ArrayList<>();
            List<TransmissionRequest> survivors = new ArrayList<>();

            // We now split the senders into two groups:
            // 1. senders whose waiting time is over, so they try now
            // 2. senders who still need to wait longer
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

            // If only one sender is ready, that sender finally gets the medium.
            if (contenders.size() == 1) {
                TransmissionRequest winner = contenders.get(0);

                // The medium now belongs only to that one sender.
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

                // Remove the winner from the pending list because it has finished.
                pendingRequests = survivors;
            } else {
                // If many senders become ready together again, they collide again.

                // That means all of them reached zero waiting time at the same moment.
                medium.beginTransmissionRound(contenders);
                System.out.println("Collision detected for " + medium.getActiveRequests().size() + " retransmission attempt(s).");
                nextTick = TransmissionTraceRenderer.renderCollision(
                        medium.getMediumName(),
                        medium.getActiveRequests(),
                        medium.getCurrentTick()
                );
                medium.advanceTicks(nextTick - medium.getCurrentTick());
                medium.clear();

                // The senders that were still waiting remain pending,
                // and the collided contenders get fresh backoff values.
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

        // Every sender that just collided now chooses a random waiting slot.
        // More collisions mean the possible waiting range becomes bigger.
        for (TransmissionRequest request : collidingRequests) {
            // Increase the retry count because this sender has seen one more collision.
            int retryCount = retryCounts.getOrDefault(request, 0) + 1;
            retryCounts.put(request, retryCount);

            // If a sender has already retried too many times, we stop retrying for it.
            if (retryCount > MAX_RETRIES) {
                System.out.println(request.getSender().getName() + " exceeded the maximum retry limit and failed to transmit.");
                backoffSlots.remove(request);
                continue;
            }

            // This is the binary exponential backoff step.
            // After each collision, the sender may need to choose from a larger range.
            int backoffRange = 1 << Math.min(retryCount, 10);

            // Pick one random slot from that range.
            // This random waiting time helps reduce the chance of another collision.
            int selectedBackoff = random.nextInt(backoffRange);
            backoffSlots.put(request, selectedBackoff);
            retryable.add(request);
            System.out.println(request.getSender().getName() + " selected backoff slot " + selectedBackoff
                    + " from range [0, " + (backoffRange - 1) + "].");
        }
        return retryable;
    }
}
