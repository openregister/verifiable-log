package uk.gov.verifiablelog.merkletree;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class MerkleTree {

    private final MessageDigest messageDigest;

    private List<byte[]> leafValues;

    public MerkleTree(MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
        leafValues = new ArrayList<>();
    }

    public byte[] currentRoot() {
        return subtreeHash(0, leafValues.size());
    }

    // hash of subtree from start (inclusive) to end (exclusive)
    private byte[] subtreeHash(int start, int end) {
        int size = end - start;
        if (size == 0) {
            return emptyTree();
        } else if (size == 1) {
            return singleLeafTree(leafValues.get(start));
        } else {
            int mid = start + k(size);
            byte[] leftSubtreeHash = subtreeHash(start, mid);
            byte[] rightSubtreeHash = subtreeHash(mid, end);
            return intermediateNode(leftSubtreeHash, rightSubtreeHash);
        }
    }

    public void addLeaf(byte[] bytes) {
        leafValues.add(bytes);
    }

    private byte[] emptyTree() {
        return messageDigest.digest();
    }

    private byte[] singleLeafTree(byte[] input) {
        messageDigest.update((byte) 0);
        return messageDigest.digest(input);
    }

    private byte[] intermediateNode(byte[] left, byte[] right) {
        messageDigest.update((byte) 1);
        messageDigest.update(left);
        return messageDigest.digest(right);
    }

    private int k(int n) {
        assert n > 1;

        int split = 1;
        do {
            split <<= 1;
        } while (split < n);

        // Get the largest power of two smaller than i.
        return split >> 1;
    }
}
