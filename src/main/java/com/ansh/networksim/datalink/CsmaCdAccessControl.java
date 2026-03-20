package com.ansh.networksim.datalink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsmaCdAccessControl {
    private static final int MAX_RETRIES = 3;

    public void simulateSlot(SharedMedium medium, List<TransmissionRequest> requests){
        if(requests.isEmpty()) return;

        System.out.println("--- CSMA/CD Slot ---");

        if(!medium.hasCollision(requests)){
            TransmissionRequest request = requests.get(0);
            System.out.println(request.getSender().getName() + " transmitted successfully without collision.");
            request.getConnection().transmitFrame(request.getSender(), request.getFrame());
            System.out.println("--- End CSMA/CD Slot ---");
            return;
        }

        System.out.println("Collision detected for " + requests.size() + " simultaneous transmissions.");

        List<TransmissionRequest> pendingRequests = new ArrayList<>(requests);
        Map<TransmissionRequest, Integer> retryCounts = new HashMap<>();
        int round = 1;

        while(!pendingRequests.isEmpty()){
            System.out.println("Backoff round " + round + ":");

            List<TransmissionRequest> nextRound = new ArrayList<>();

            for(int i = 0; i < pendingRequests.size(); i++){
                TransmissionRequest current = pendingRequests.get(i);
                int retryCount = retryCounts.getOrDefault(current, 0) + 1;
                retryCounts.put(current, retryCount);

                if(retryCount > MAX_RETRIES){
                    System.out.println(current.getSender().getName() + " exceeded the maximum retry limit and failed to transmit.");
                    continue;
                }

                System.out.println("Retry attempt " + retryCount + " for " + current.getSender().getName() + ".");

                if(i == 0){
                    System.out.println(current.getSender().getName() + " successfully transmitted after backoff.");
                    current.getConnection().transmitFrame(current.getSender(), current.getFrame());
                }else {
                    System.out.println(current.getSender().getName() + " must wait for the next backoff round.");
                    nextRound.add(current);
                }
            }

            pendingRequests = nextRound;
            round++;
        }

        System.out.println("--- End CSMA/CD Slot ---");
    }
}
