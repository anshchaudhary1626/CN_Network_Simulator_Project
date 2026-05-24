package com.ansh.networksim.simulation;

/**
 * Normalizes plain-text and raw-bit payloads so the rest of the simulator can handle both forms.
 */
public final class PayloadUtil {
    private static final String BITS_PREFIX = "BITS:";

    // Prevent instantiation of this utility class.
    private PayloadUtil() {
    }

    // Bit payloads are tagged internally so the code can distinguish them from ordinary text.
    public static String fromBits(String bits) {
        return BITS_PREFIX + bits;
    }

    // Check whether the payload is already tagged as raw bits.
    public static boolean isBitPayload(String payload) {
        return payload.startsWith(BITS_PREFIX);
    }

    // Remove the internal bit prefix before showing payloads to the user.
    public static String display(String payload) {
        if (isBitPayload(payload)) {
            return payload.substring(BITS_PREFIX.length());
        }
        return payload;
    }

    // Convert text payloads to bits while leaving raw bit payloads unchanged.
    public static String toPayloadBits(String payload) {
        if (isBitPayload(payload)) {
            return payload.substring(BITS_PREFIX.length());
        }

        StringBuilder builder = new StringBuilder();
        // Encode each character as an 8-bit binary value.
        for (int index = 0; index < payload.length(); index++) {
            builder.append(encodeFixedWidth(payload.charAt(index), 8));
        }
        return builder.toString();
    }

    // Left-pad binary values so every encoded unit has the same width.
    private static String encodeFixedWidth(int value, int width) {
        String binary = Integer.toBinaryString(value);
        if (binary.length() > width) {
            return binary.substring(binary.length() - width);
        }

        return "0".repeat(width - binary.length()) + binary;
    }
}
