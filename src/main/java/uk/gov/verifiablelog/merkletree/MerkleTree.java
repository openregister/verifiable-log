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
        this.messageDigest = messageDigest;
        this.leafDAOFunction = leafDAOFunction;
        this.leafSizeDAOFunction = leafSizeDAOFunction;
        this.memoizationStore = new DoNothing();
    }

    public MerkleTree(MessageDigest messageDigest, Function<Integer, byte[]> leafDAOFunction, Supplier<Integer> leafSizeDAOFunction, MemoizationStore memoizationStore) {
        this.messageDigest = messageDigest;
        this.leafDAOFunction = leafDAOFunction;
        this.leafSizeDAOFunction = leafSizeDAOFunction;
        this.memoizationStore = memoizationStore;
    }

    public byte[] currentRoot() {
        return subtreeHash(0, leafSizeDAOFunction.get());
    }

//    public List<byte[]> pathToRootAtSnapshot(int leafIndex, int snapshotSize) {
//        return subtreePathAtSnapshot(leafIndex, snapshotSize, leafDAOFunction);
//    }
//
//    public List<byte[]> snapshotConsistency(int snapshot1, int snapshot2) {
//        if (snapshot1 <= 0) {
//            // RFC 6962 §2.1.2 assumes `0 < m < n`; we assume `0 < m <= n`
//            throw new IllegalArgumentException("snapshot1 must be strictly positive");
//        }
//        return subtreeSnapshotConsistency(snapshot1, snapshot2, true, leafDAOFunction);
//    }
//
//    private List<byte[]> subtreeSnapshotConsistency(int low, int high, boolean startFromOldRoot, Function<Integer, byte[]> subtreeDAOFunction) {
//        if (low == high) {
//            if (startFromOldRoot) {
//                // this is the b == true case in RFC 6962
//                return new ArrayList<>();
//            }
//            List<byte[]> consistencySet = new ArrayList<>();
//            consistencySet.add(realSubtreeHash(high, subtreeDAOFunction));
//            return consistencySet;
//        }
//        int k = Util.k(high);
//        if (low <= k) {
//            List<byte[]> subtreeConsistencySet = subtreeSnapshotConsistency(low, k, startFromOldRoot, subtreeDAOFunction);
//            subtreeConsistencySet.add(realSubtreeHash(high - k, i -> subtreeDAOFunction.apply(i + k)));
//            return subtreeConsistencySet;
//        } else {
//            List<byte[]> subtreeConsistencySet = subtreeSnapshotConsistency(low - k, high - k, false, i -> subtreeDAOFunction.apply(i + k));
//            subtreeConsistencySet.add(realSubtreeHash(k, subtreeDAOFunction));
//            return subtreeConsistencySet;
//        }
//    }
//
//    // audit path within subtree of leaves from start (inclusive) to end (exclusive)
//    private List<byte[]> subtreePathAtSnapshot(int leafIndex, int snapshotSize, Function<Integer, byte[]> subtreeDAOFunction) {
//        if (snapshotSize <= 1) {
//            return new ArrayList<>();
//        }
//        int k = Util.k(snapshotSize);
//        if (leafIndex < k) {
//            List<byte[]> subtreePath = subtreePathAtSnapshot(leafIndex, k, subtreeDAOFunction);
//            subtreePath.add(realSubtreeHash(snapshotSize - k, i -> subtreeDAOFunction.apply(k + i)));
//            return subtreePath;
//        } else {
//            List<byte[]> subtreePath = subtreePathAtSnapshot(leafIndex - k, snapshotSize - k, i -> subtreeDAOFunction.apply(i + k));
//            subtreePath.add(realSubtreeHash(k, subtreeDAOFunction));
//            return subtreePath;
//        }
//    }

    // hash of subtree of given size
    private byte[] realSubtreeHash(int n, int size) {
        if (size == 0) {
            return emptyTree();
        } else if (size == 1) {
            return Util.leafHash(leafDAOFunction.apply(n), messageDigest);
        } else {
            int k = Util.k(size);
            byte[] leftSubtreeHash = subtreeHash(n, k);
            byte[] rightSubtreeHash = subtreeHash(k + n, size - k);
            return Util.branchHash(leftSubtreeHash, rightSubtreeHash, messageDigest);
        }
    }

    private byte[] subtreeHash(int n, int size) {
        byte[] result = memoizationStore.get(n, size);

        if (result != null) {
            return result;
        } else {
            byte[] realResult = realSubtreeHash(n, size);
            memoizationStore.put(n, size, realResult);
            return realResult;
        }

    }

    private byte[] emptyTree() {
        return messageDigest.digest();
    }
}


