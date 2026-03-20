package com.ansh.networksim.datalink;

public class Frame {
    private final String sourceMac;
    private final String destinationMac;
    private final int sequenceNumber;
    private final String payload;
    private final int checksum;
    private final FrameType type;

    public static final String BROADCAST_MAC = "FF:FF";

    public Frame(String sourceMac, String destinationMac, int sequenceNumber, String payload, int checksum, FrameType type) {
        this.sourceMac = sourceMac;
        this.destinationMac = destinationMac;
        this.sequenceNumber = sequenceNumber;
        this.payload = payload;
        this.checksum = checksum;
        this.type = type;
    }

    public static Frame createDataFrame(String sourceMac, String destinationMac, int sequenceNumber, String payload) {
        int checksum = ChecksumUtil.computeChecksum(payload);
        return new Frame(sourceMac, destinationMac, sequenceNumber, payload, checksum, FrameType.DATA);
    }

    public static Frame createAckFrame(String sourceMac, String destinationMac, int sequenceNumber){
        return new Frame(sourceMac, destinationMac, sequenceNumber, "", 0, FrameType.ACK);
    }

    public boolean isValid(){
        if(type == FrameType.ACK) return true;

        return checksum == ChecksumUtil.computeChecksum(payload);
    }

    public String getSourceMac() {
        return sourceMac;
    }

    public String getDestinationMac() {
        return destinationMac;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getPayload() {
        return payload;
    }

    public Frame corruptPayload() {
        return new Frame(sourceMac, destinationMac, sequenceNumber, payload + "?", checksum, type);
    }

    public int getChecksum() {
        return checksum;
    }

    public FrameType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "sourceMac='" + sourceMac + '\'' +
                ", destinationMac='" + destinationMac + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", payload='" + payload + '\'' +
                ", checksum=" + checksum +
                ", type=" + type +
                '}';
    }
}