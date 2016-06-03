package uk.gov.verifiablelog.merkletree;

import java.security.MessageDigest;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class MerkleTree {

    private final MessageDigest messageDigest;

    private final BiFunction<Integer, Integer, Iterator<byte[]>> leafDAOFunction;
    private final Supplier<Integer> leafSizeDAOFunction;


    public MerkleTree(MessageDigest messageDigest, BiFunction<Integer, Integer, Iterator<byte[]>> leafDAOFunction, Supplier<Integer> leafSizeDAOFunction) {
        this.messageDigest = messageDigest;
        this.leafDAOFunction = leafDAOFunction;
        this.leafSizeDAOFunction = leafSizeDAOFunction;
    }

    public byte[] currentRoot() {
        return subtreeHash(0, leafSizeDAOFunction.get(), leafDAOFunction.apply(0, leafSizeDAOFunction.get()));
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
            consistencySet.add(subtreeHash(start, end, leafDAOFunction.apply(start, end)));
            return consistencySet;
        }
        int k = Util.k(size);
        int mid = start + k;
        if (m <= k) {
            List<byte[]> subtreeConsistencySet = subtreeSnapshotConsistency(start, mid, m);
            subtreeConsistencySet.add(subtreeHash(mid, end, leafDAOFunction.apply(mid, end)));
            return subtreeConsistencySet;
        } else {
            List<byte[]> subtreeConsistencySet = subtreeSnapshotConsistency(mid, end, m - k);
            subtreeConsistencySet.add(subtreeHash(start, mid, leafDAOFunction.apply(start, mid)));
            return subtreeConsistencySet;
        }
    }

    // audit path within subtree of leaves from start (inclusive) to end (exclusive)
    private List<byte[]> subtreePathAtSnapshot(int leafIndex, int start, int end) {
        int size = end - start;
        if (size <= 1) {
            return new ArrayList<>();
        }
        int mid = start + Util.k(size);
        if (leafIndex < mid) {
            List<byte[]> subtreePath = subtreePathAtSnapshot(leafIndex, start, mid);
            subtreePath.add(subtreeHash(mid, end, leafDAOFunction.apply(mid, end)));
            return subtreePath;
        } else {
            List<byte[]> subtreePath = subtreePathAtSnapshot(leafIndex, mid, end);
            subtreePath.add(subtreeHash(start, mid, leafDAOFunction.apply(start, mid)));
            return subtreePath;
        }
    }

    // hash of subtree from start (inclusive) to end (exclusive)
    private byte[] subtreeHash(int start, int end, Iterator<byte[]> iterator) {
        int size = end - start;
        if (size == 0) {
            return emptyTree();
        } else if (size == 1) {
            return Util.leafHash(iterator.next(), messageDigest);
        } else {
            int mid = start + Util.k(size);
            byte[] leftSubtreeHash = subtreeHash(start, mid, iterator);
            byte[] rightSubtreeHash = subtreeHash(mid, end, iterator);
            return Util.branchHash(leftSubtreeHash, rightSubtreeHash, messageDigest);
        }
    }

    private byte[] emptyTree() {
        return messageDigest.digest();
    }
}
