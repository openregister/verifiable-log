package uk.gov.verifiablelog.merkletree;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {
    public static int k(int n) {
        assert n > 1;

        int split = 1;
        do {
            split <<= 1;
        } while (split < n);

        // Get the largest power of two smaller than i.
        return split >> 1;
    }

    static byte[] branchHash(byte[] left, byte[] right, MessageDigest digest) {
        digest.update(new byte[]{0x01});
        digest.update(left);
        return digest.digest(right);
    }

    static byte[] leafHash(byte[] leafData, MessageDigest digest) {
        digest.update(new byte[]{0x00});
        return digest.digest(leafData);
    }

    static MessageDigest sha256Instance() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("can't happen", e);
        }
    }
}
