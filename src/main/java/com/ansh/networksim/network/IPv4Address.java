package com.ansh.networksim.network;

import java.util.Objects;

/**
 * Represents an IPv4 address written in CIDR format.
 *
 * Example:
 * - 192.168.1.10/24
 *
 * In simple words:
 * - 192.168.1.10 is the device address.
 * - /24 tells how much of the address is the network part.
 * - Devices with the same network part are in the same local network.
 *
 * This class is immutable, meaning once we create it, its value does not change.
 */
public final class IPv4Address {
    private final String address;
    private final int prefixLength;
    private final int value;

    public IPv4Address(String cidrNotation) {
        // First check that the text looks like "number.number.number.number/prefix".
        if (cidrNotation == null || !cidrNotation.matches("\\d{1,3}(\\.\\d{1,3}){3}/\\d{1,2}")) {
            throw new IllegalArgumentException("IPv4 address must use CIDR notation, for example 192.168.1.10/24");
        }

        String[] parts = cidrNotation.split("/");
        int parsedPrefix = Integer.parseInt(parts[1]);
        // IPv4 has 32 bits, so prefix length can only be 0 to 32.
        if (parsedPrefix < 0 || parsedPrefix > 32) {
            throw new IllegalArgumentException("IPv4 prefix length must be between 0 and 32");
        }

        // Store both text form and integer form. Integer form makes masking easy.
        this.value = parseAddress(parts[0]);
        this.address = parts[0];
        this.prefixLength = parsedPrefix;
    }

    public IPv4Address(String address, int prefixLength) {
        this(address + "/" + prefixLength);
    }

    private static int parseAddress(String address) {
        // Split 192.168.1.10 into four octets: 192, 168, 1, 10.
        String[] octets = address.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("IPv4 address must contain four octets");
        }

        int parsed = 0;
        for (String octet : octets) {
            int value = Integer.parseInt(octet);
            // Each octet is one byte, so it must fit from 0 to 255.
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException("IPv4 octets must be between 0 and 255");
            }
            // Shift old bits left and add the next octet.
            parsed = (parsed << 8) | value;
        }
        return parsed;
    }

    private static String formatAddress(int value) {
        // Convert the 32-bit integer form back into dotted decimal form.
        return ((value >>> 24) & 0xFF) + "."
                + ((value >>> 16) & 0xFF) + "."
                + ((value >>> 8) & 0xFF) + "."
                + (value & 0xFF);
    }

    private static int maskForPrefix(int prefixLength) {
        // /0 means no network bits, so the mask is 0.0.0.0.
        if (prefixLength == 0) {
            return 0;
        }
        // Example: /24 becomes 255.255.255.0.
        return (int) (0xFFFFFFFFL << (32 - prefixLength));
    }

    public String getAddress() {
        return address;
    }

    public int getPrefixLength() {
        return prefixLength;
    }

    public int asInt() {
        return value;
    }

    public String getNetworkAddress() {
        // Apply the subnet mask. Example: 192.168.1.10/24 -> 192.168.1.0.
        return formatAddress(value & maskForPrefix(prefixLength));
    }

    public String getSubnetMask() {
        // Convert the prefix length into normal mask text.
        return formatAddress(maskForPrefix(prefixLength));
    }

    public boolean isSameNetwork(IPv4Address other) {
        // Two addresses are in the same network if their masked network bits match.
        Objects.requireNonNull(other, "other");
        int mask = maskForPrefix(prefixLength);
        return (value & mask) == (other.value & mask);
    }

    public boolean contains(String destinationIp) {
        // Used by routes: check whether a destination IP belongs inside this route network.
        int destination = parseAddress(destinationIp);
        int mask = maskForPrefix(prefixLength);
        return (value & mask) == (destination & mask);
    }

    public IPv4Address networkPrefix() {
        // Convert host address to network address while keeping the same prefix.
        return new IPv4Address(getNetworkAddress(), prefixLength);
    }

    @Override
    public String toString() {
        return address + "/" + prefixLength;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IPv4Address other)) {
            return false;
        }
        return value == other.value && prefixLength == other.prefixLength;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, prefixLength);
    }
}
