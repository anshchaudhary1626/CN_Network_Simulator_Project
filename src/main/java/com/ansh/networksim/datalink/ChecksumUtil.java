package com.ansh.networksim.datalink;

public final class ChecksumUtil{
    private ChecksumUtil(){

    }
    public static int computeChecksum(String data){
        int sum = 0;
        for(int i = 0; i < data.length(); i++){
            char ch = data.charAt(i);
            sum = (sum + ch) % 256;
        }

        return sum;
    }
}