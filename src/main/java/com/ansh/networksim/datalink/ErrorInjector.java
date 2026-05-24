package com.ansh.networksim.datalink;

import java.util.HashSet;
import java.util.Set;

/**
 * Corrupts selected data-frame sequence numbers once so error handling can be demonstrated.
 */
public class ErrorInjector {
    private final Set<Integer> corruptedSequences;

    // Copy the configured sequence numbers so external callers cannot mutate them later.
    public ErrorInjector(Set<Integer> corruptedSequences) {
        this.corruptedSequences = new HashSet<>(corruptedSequences);
    }

    // The sequence is removed after corruption so the same frame is not damaged repeatedly.
    public Frame maybeCorrupt(Frame frame){
        if (corruptedSequences.remove(frame.getSequenceNumber()) && frame.getType() == FrameType.DATA) {
            return frame.corruptPayload();
        }
        return frame;
    }
}
