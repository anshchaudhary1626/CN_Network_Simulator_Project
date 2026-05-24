package com.ansh.networksim.simulation;

/**
 * Small wrapper around a binary string plus a human-readable layout description.
 */
public class BitStream {
    private final String layout;
    private final String bits;

    // Store the descriptive layout and the actual bit sequence.
    public BitStream(String layout, String bits) {
        this.layout = layout;
        this.bits = bits;
    }

    // Return the labeled field layout.
    public String getLayout() {
        return layout;
    }

    // Return the raw bit string.
    public String getBits() {
        return bits;
    }

    // Return the total number of bits.
    public int length() {
        return bits.length();
    }

    // Return one bit at the requested position.
    public char bitAt(int index) {
        return bits.charAt(index);
    }
}
