package com.ansh.networksim.datalink;

import java.nio.charset.StandardCharsets;

/**
 * This helper makes the checksum for a payload.
 * We later use that checksum to check whether the payload got changed or corrupted.
 */
public final class ChecksumUtil{
    // This class is only a helper, so we do not create objects from it.
    private ChecksumUtil(){

    }

    // This method takes the payload text and turns it into one checksum number.
    public static int computeChecksum(String data){
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        int sum = 0;

        // We read the data 2 bytes at a time.
        // Two bytes together make one 16-bit block, which is what this checksum method uses.
        for (int index = 0; index < bytes.length; index += 2) {
            // First we take the current byte and place it in the left half of the 16-bit block.
            int word = (bytes[index] & 0xFF) << 8;

            // If there is one more byte available, we place it in the right half.
            // If not, we simply leave that right half as 0.
            if (index + 1 < bytes.length) {
                word |= bytes[index + 1] & 0xFF;
            }

            // Now we add this 16-bit block into our running total.
            // So this line is the actual "adding" step.
            sum += word;

            // Sometimes the total becomes bigger than 16 bits after adding.
            // When that happens, the extra carry bit is not thrown away.
            // Instead, we "wrap it around" and add it back into the lower 16 bits.
            // This loop is the wrapped-sum step.
            while ((sum & 0xFFFF0000) != 0) {
                sum = (sum & 0xFFFF) + (sum >>> 16);
            }
        }

        // After all the blocks have been added, we flip all the bits of the final sum.
        // That flipped value is called the complement.
        // This final returned number is the checksum we store in the frame.
        return (~sum) & 0xFFFF;
    }
}
