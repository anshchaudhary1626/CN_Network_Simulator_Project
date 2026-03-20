package com.ansh.networksim.datalink;

import java.util.HashSet;
import java.util.Set;

public class ErrorInjector {
    private final Set<Integer> corruptedSequences;

    public ErrorInjector(Set<Integer> corruptedSequences) {
        this.corruptedSequences = new HashSet<>(corruptedSequences);
    }

    public Frame maybeCorrupt(Frame frame){
        if (corruptedSequences.remove(frame.getSequenceNumber()) && frame.getType() == FrameType.DATA) {
            return frame.corruptPayload();
        }
        return frame;
    }
}
