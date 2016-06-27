package uk.gov.verifiablelog.merkletree;

import java.security.MessageDigest;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class MerkleTree {

    private final MessageDigest messageDigest;

    private final Function<Integer, byte[]> leafDAOFunction;
    private final Supplier<Integer> leafSizeDAOFunction;
    private final MemoizationStore memoizationStore;

    public MerkleTree(MessageDigest messageDigest, Function<Integer, byte[]> leafDAOFunction, Supplier<Integer> leafSizeDAOFunction) {
        this(messageDigest, leafDAOFunction, leafSizeDAOFunction, null);
    }

    public MerkleTree(MessageDigest messageDigest, Function<Integer, byte[]> leafDAOFunction, Supplier<Integer> leafSizeDAOFunction, MemoizationStore memoizationStore) {
        this.messageDigest = messageDigest;
        this.leafDAOFunction = leafDAOFunction;
        this.leafSizeDAOFunction = leafSizeDAOFunction;
        this.memoizationStore = memoizationStore == null ? new DoNothing(): memoizationStore;
    }

    public byte[] currentRoot() {
        return subtreeHash(0, leafSizeDAOFunction.get());
    }

    public List<byte[]> pathToRootAtSnapshot(int leafIndex, int snapshotSize) {
        return subtreePathAtSnapshot(leafIndex, 0, snapshotSize);
    }

    public List<byte[]> snapshotConsistency(int snapshot1, int snapshot2) {
        if (snapshot1 <= 0) {
            // RFC 6962 §2.1.2 assumes `0 < m < n`; we assume `0 < m <= n`
            throw new IllegalArgumentException("snapshot1 must be strictly positive");
        }
        return subtreeSnapshotConsistency(snapshot1, snapshot2, 0, true);
    }

    private List<byte[]> subtreeSnapshotConsistency(int low, int high, int start, boolean startFromOldRoot) {
        if (low == high) {
            if (startFromOldRoot) {
                // this is the b == true case in RFC 6962
                return new ArrayList<>();
            }
            List<byte[]> consistencySet = new ArrayList<>();
            consistencySet.add(subtreeHash(start, high));
            return consistencySet;
        }
        int k = Util.k(high);
        if (low <= k) {
            List<byte[]> subtreeConsistencySet = subtreeSnapshotConsistency(low, k, start, startFromOldRoot);
            subtreeConsistencySet.add(subtreeHash(start + k, high - k));
            return subtreeConsistencySet;
        } else {
            List<byte[]> subtreeConsistencySet = subtreeSnapshotConsistency(low - k, high - k, start + k, false);
            subtreeConsistencySet.add(subtreeHash(start, k));
            return subtreeConsistencySet;
        }
    }

    // audit path within subtree of leaves from start (inclusive) to end (exclusive)
    private List<byte[]> subtreePathAtSnapshot(int leafIndex, int start, int snapshotSize) {
        if (snapshotSize <= 1) {
            return new ArrayList<>();
        }
        int k = Util.k(snapshotSize);
        if (leafIndex < k) {
            List<byte[]> subtreePath = subtreePathAtSnapshot(leafIndex, start, k);
            subtreePath.add(subtreeHash(start + k, snapshotSize - k));
            return subtreePath;
        } else {
            List<byte[]> subtreePath = subtreePathAtSnapshot(leafIndex - k, start + k, snapshotSize - k);
            subtreePath.add(subtreeHash(start, k));
            return subtreePath;
        }
    }

    // hash of subtree of given size
    private byte[] computeSubtreeHash(int start, int size) {
        if (size == 0) {
            return emptyTreeHash();
        } else if (size == 1) {
            return Util.leafHash(leafDAOFunction.apply(start), messageDigest);
        } else {
            int k = Util.k(size);
            byte[] leftSubtreeHash = subtreeHash(start, k);
            byte[] rightSubtreeHash = subtreeHash(k + start, size - k);
            return Util.branchHash(leftSubtreeHash, rightSubtreeHash, messageDigest);
        }
    }

    private byte[] subtreeHash(int start, int size) {
        byte[] result = memoizationStore.get(start, size);

        if (result != null) {
            return result;
        } else {
            byte[] realResult = computeSubtreeHash(start, size);
            memoizationStore.put(start, size, realResult);
            return realResult;
        }

    }

    private byte[] emptyTreeHash() {
        return messageDigest.digest();
    }
}


