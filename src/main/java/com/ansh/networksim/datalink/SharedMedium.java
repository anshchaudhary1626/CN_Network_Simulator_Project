package com.ansh.networksim.datalink;

import java.util.List;

public class SharedMedium {
    public boolean hasCollision(List<TransmissionRequest> requests){
        return requests.size() > 1;
    }
}