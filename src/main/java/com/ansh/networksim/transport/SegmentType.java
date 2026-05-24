package com.ansh.networksim.transport;

/**
 * Identifies what kind of transport message this is.
 *
 * DATA: carries actual application content.
 * ACK: confirms that data was received.
 */
public enum SegmentType {
    DATA,
    ACK
}
