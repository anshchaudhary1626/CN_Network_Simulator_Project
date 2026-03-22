package com.ansh.networksim.simulation;

import com.ansh.networksim.datalink.Frame;
import com.ansh.networksim.datalink.FrameType;

public final class FrameSerializer {
    private static final String FLAG = "01111110";

    private FrameSerializer() {
    }

    public static BitStream serialize(Frame frame) {
        String sourceBits = encodeText(frame.getSourceMac());
        String destinationBits = encodeText(frame.getDestinationMac());
        String typeBits = frame.getType() == FrameType.ACK ? "1" : "0";
        String sequenceBits = encodeInt(frame.getSequenceNumber());
        String payloadBits = PayloadUtil.toPayloadBits(frame.getPayload());
        String checksumBits = encodeInt(frame.getChecksum());

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

    private static String encodeText(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            builder.append(encodeFixedWidth(value.charAt(i), 8));
        }
        return builder.toString();
    }

    private static String encodeInt(int value) {
        return encodeFixedWidth(value & 0xFF, 8);
    }

    private static String encodeFixedWidth(int value, int width) {
        String binary = Integer.toBinaryString(value);
        if (binary.length() > width) {
            return binary.substring(binary.length() - width);
        }

        return "0".repeat(width - binary.length()) + binary;
    }
}
