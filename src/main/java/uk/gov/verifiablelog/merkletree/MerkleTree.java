package uk.gov.verifiablelog.merkletree;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MerkleTree {

    private final MessageDigest messageDigest;

    private Function<Integer, byte[]> leafDAOFunction;
    private Supplier<Integer> leafSizeDAOFunction;

    public MerkleTree(MessageDigest messageDigest, Function<Integer, byte[]> leafDAOFunction, Supplier<Integer> leafSizeDAOFunction) {
        this.messageDigest = messageDigest;
        this.leafDAOFunction = leafDAOFunction;
        this.leafSizeDAOFunction = leafSizeDAOFunction;
    }

    public byte[] currentRoot() {
        return subtreeHash(0, leafSizeDAOFunction.get());
    }

    public List<byte[]> pathToRootAtSnapshot(int leafIndex, int snapshotSize) {
        return subtreePathAtSnapshot(leafIndex, 0, snapshotSize);
    }

    public List<byte[]> snapshotConsistency(int snapshot1, int snapshot2) {
        return subtreeSnapshotConsistency(0, snapshot2, snapshot1);
    }

    private List<byte[]> subtreeSnapshotConsistency(int start, int end, int m) {
        int size = end - start;
        if (m == size) {
            if (start == 0) {
                // this is the b == true case in RFC 6962
                return new ArrayList<>();
            }
            List<byte[]> consistencySet = new ArrayList<>();
            consistencySet.add(subtreeHash(start,end));
            return consistencySet;
        }
        int k = k(size);
        int mid = start + k;
        if (m <= k) {
            List<byte[]> subtreeConsistencySet = subtreeSnapshotConsistency(start, mid, m);
            subtreeConsistencySet.add(subtreeHash(mid, end));
            return subtreeConsistencySet;
        } else {
            List<byte[]> subtreeConsistencySet = subtreeSnapshotConsistency(mid, end, m - k);
            subtreeConsistencySet.add(subtreeHash(start, mid));
            return subtreeConsistencySet;
        }
    }

    // audit path within subtree of leaves from start (inclusive) to end (exclusive)
    private List<byte[]> subtreePathAtSnapshot(int leafIndex, int start, int end) {
        int size = end - start;
        if (size <= 1) {
            return new ArrayList<>();
        }
        int mid = start + k(size);
        if (leafIndex < mid) {
            List<byte[]> subtreePath = subtreePathAtSnapshot(leafIndex, start, mid);
            subtreePath.add(subtreeHash(mid, end));
            return subtreePath;
        } else {
            List<byte[]> subtreePath = subtreePathAtSnapshot(leafIndex, mid, end);
            subtreePath.add(subtreeHash(start, mid));
            return subtreePath;
        }
    }

    // hash of subtree from start (inclusive) to end (exclusive)
    private byte[] subtreeHash(int start, int end) {
        int size = end - start;
        if (size == 0) {
            return emptyTree();
        } else if (size == 1) {
            return singleLeafTree(leafDAOFunction.apply(start));
        } else {
            int mid = start + k(size);
            byte[] leftSubtreeHash = subtreeHash(start, mid);
            byte[] rightSubtreeHash = subtreeHash(mid, end);
            return intermediateNode(leftSubtreeHash, rightSubtreeHash);
        }
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
