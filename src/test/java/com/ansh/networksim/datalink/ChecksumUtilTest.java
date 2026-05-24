package com.ansh.networksim.datalink;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sanity checks for checksum calculation and frame validation behavior.
 */
class ChecksumUtilTest {

    // Verify a known payload produces the expected checksum constant.
    @Test
    void computeChecksumUsesInternetChecksumForAsciiPayload() {
        assertEquals(56365, ChecksumUtil.computeChecksum("Hello"));
    }

    // Verify checksum validation fails after the payload is altered.
    @Test
    void dataFrameValidationFailsAfterPayloadCorruption() {
        Frame frame = Frame.createDataFrame("S1", "S2", 7, "P2");

        assertTrue(frame.isValid());
        assertFalse(frame.corruptPayload().isValid());
    }
}
