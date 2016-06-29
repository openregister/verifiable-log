package uk.gov.verifiablelog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A utility class that provides commonly used operations when working with Merkle Trees.
 */
class Util {

    /**
     * Calculates the largest power of two smaller than the given value.
     * @param n An integer that must be greater than 1
     * @return The largest power of two smaller than n
     */
    protected static int k(int n) {
        if (n < 2) {
            throw new IllegalArgumentException("n must be greater than 1");
        }

        int split = 1;
        do {
            split <<= 1;
        } while (split < n);

        // Get the largest power of two smaller than i.
        return split >> 1;
    }

    /**
     * Calculates the combined Merkle Tree hash of two child Merkle Tree nodes.
     * @param left The value of the left-hand Merkle Tree node
     * @param right The value of the right-hand Merkle Tree node
     * @param digest The algorithm to use when creating hash values of Merkle Tree nodes
     * @return A Merkle Tree hash
     */
    protected static byte[] branchHash(byte[] left, byte[] right, MessageDigest digest) {
        digest.update((byte) 0x01);
        digest.update(left);
        digest.update(right);
        return digest.digest();
    }

    /**
     * Calculates the Merkle Tree hash from the raw data of a single leaf.
     * @param leafData The raw value of the leaf data
     * @param digest The algorithm to use when creating a hash value of the leaf data
     * @return A merkle tree hash
     */
    protected static byte[] leafHash(byte[] leafData, MessageDigest digest) {
        digest.update((byte) 0x00);
        digest.update(leafData);
        return digest.digest();
    }

    /**
     * An instance of the sha-256 algorithm for message digest.
     * @return A new sha-256 message digest
     */
    protected static MessageDigest sha256Instance() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("can't happen", e);
        }
    }
}
