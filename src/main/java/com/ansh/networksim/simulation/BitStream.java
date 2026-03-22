package com.ansh.networksim.simulation;

public class BitStream {
    private final String layout;
    private final String bits;

    public BitStream(String layout, String bits) {
        this.layout = layout;
        this.bits = bits;
    }

    public String getLayout() {
        return layout;
    }

    public String getBits() {
        return bits;
    }

    public int length() {
        return bits.length();
    }

    public char bitAt(int index) {
        return bits.charAt(index);
    }
}
