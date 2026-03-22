package com.ansh.networksim.simulation;

public final class PayloadUtil {
    private static final String BITS_PREFIX = "BITS:";

    private PayloadUtil() {
    }

    public static String fromBits(String bits) {
        return BITS_PREFIX + bits;
    }

    public static boolean isBitPayload(String payload) {
        return payload.startsWith(BITS_PREFIX);
    }

    public static String display(String payload) {
        if (isBitPayload(payload)) {
            return payload.substring(BITS_PREFIX.length());
        }
        return payload;
    }

    public static String toPayloadBits(String payload) {
        if (isBitPayload(payload)) {
            return payload.substring(BITS_PREFIX.length());
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < payload.length(); index++) {
            builder.append(encodeFixedWidth(payload.charAt(index), 8));
        }
        return builder.toString();
    }

    private static String encodeFixedWidth(int value, int width) {
        String binary = Integer.toBinaryString(value);
        if (binary.length() > width) {
            return binary.substring(binary.length() - width);
        }

        return "0".repeat(width - binary.length()) + binary;
    }
}
