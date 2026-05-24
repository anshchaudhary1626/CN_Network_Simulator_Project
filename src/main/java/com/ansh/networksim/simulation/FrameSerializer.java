package com.ansh.networksim.simulation;

import com.ansh.networksim.datalink.Frame;
import com.ansh.networksim.datalink.FrameType;

/**
 * Converts a frame into a full bit-level representation for visualization.
 */
public final class FrameSerializer {
    private static final String FLAG = "01111110";

    // Prevent instantiation of this serializer helper.
    private FrameSerializer() {
    }

    // The current demos mostly render payload bits, but the full serialized layout is kept available.
    public static BitStream serialize(Frame frame) {
        String sourceBits = encodeText(frame.getSourceMac());
        String destinationBits = encodeText(frame.getDestinationMac());
        String typeBits = frame.getType() == FrameType.ACK ? "1" : "0";
        String sequenceBits = encodeInt(frame.getSequenceNumber());
        String payloadBits = PayloadUtil.toPayloadBits(frame.getPayload());
        String checksumBits = encodeChecksum(frame.getChecksum());

        String layout = "FLAG | SRC | DST | TYPE | SEQ | PAYLOAD | CRC | FLAG";
        String bits = FLAG
                + sourceBits
                + destinationBits
                + typeBits
                + sequenceBits
                + payloadBits
                + checksumBits
                + FLAG;

        return new BitStream(layout, bits);
    }

    // Encode every character of a text field as 8-bit binary.
    private static String encodeText(String value) {
        StringBuilder builder = new StringBuilder();
        // Serialize each character in order so the field layout is predictable.
        for (int i = 0; i < value.length(); i++) {
            builder.append(encodeFixedWidth(value.charAt(i), 8));
        }
        return builder.toString();
    }

    // Encode an 8-bit integer field such as sequence number.
    private static String encodeInt(int value) {
        return encodeFixedWidth(value & 0xFF, 8);
    }

    // Encode the 16-bit checksum field.
    private static String encodeChecksum(int value) {
        return encodeFixedWidth(value & 0xFFFF, 16);
    }

    // Left-pad a value so it occupies a fixed number of bits.
    private static String encodeFixedWidth(int value, int width) {
        String binary = Integer.toBinaryString(value);
        if (binary.length() > width) {
            return binary.substring(binary.length() - width);
        }

        return "0".repeat(width - binary.length()) + binary;
    }
}
