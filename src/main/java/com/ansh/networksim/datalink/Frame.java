package com.ansh.networksim.datalink;

/**
 * Basic data-link unit carrying addressing, sequencing, payload, and checksum metadata.
 */
public class Frame {
    private final String sourceMac;
    private final String destinationMac;
    private final int sequenceNumber;
    private final String payload;
    private final int checksum;
    private final FrameType type;

    public static final String BROADCAST_MAC = "FF:FF";

    // Build a frame from fully prepared field values.
    public Frame(String sourceMac, String destinationMac, int sequenceNumber, String payload, int checksum, FrameType type) {
        this.sourceMac = sourceMac;
        this.destinationMac = destinationMac;
        this.sequenceNumber = sequenceNumber;
        this.payload = payload;
        this.checksum = checksum;
        this.type = type;
    }

    // Create a DATA frame and compute its checksum from the payload.
    public static Frame createDataFrame(String sourceMac, String destinationMac, int sequenceNumber, String payload) {
        int checksum = ChecksumUtil.computeChecksum(payload);
        return new Frame(sourceMac, destinationMac, sequenceNumber, payload, checksum, FrameType.DATA);
    }

    // Create an ACK frame with an empty payload.
    public static Frame createAckFrame(String sourceMac, String destinationMac, int sequenceNumber){
        return new Frame(sourceMac, destinationMac, sequenceNumber, "", 0, FrameType.ACK);
    }

    // ACK frames are treated as control messages and are always considered valid in this simulation.
    public boolean isValid(){
        if(type == FrameType.ACK) return true;

        return checksum == ChecksumUtil.computeChecksum(payload);
    }

    // Expose the learned source address.
    public String getSourceMac() {
        return sourceMac;
    }

    // Expose the destination address.
    public String getDestinationMac() {
        return destinationMac;
    }

    // Expose the sequence number used by ARQ protocols.
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    // Expose the payload as stored in the frame.
    public String getPayload() {
        return payload;
    }

    // Corruption changes the payload but keeps the original checksum so validation will fail later.
    public Frame corruptPayload() {
        return new Frame(sourceMac, destinationMac, sequenceNumber, payload + "?", checksum, type);
    }

    // Expose the checksum field for serialization and debugging.
    public int getChecksum() {
        return checksum;
    }

    // Expose whether this is a DATA or ACK frame.
    public FrameType getType() {
        return type;
    }

    // Render the frame fields for logs and debugging output.
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
