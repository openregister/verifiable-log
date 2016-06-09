package uk.gov.verifiablelog.merkletree;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.verifiablelog.merkletree.Util.branchHash;
import static uk.gov.verifiablelog.merkletree.Util.k;

public class MerkleTreeVerification {
    static boolean isValidAuditProof(byte[] expectedRootHash, int treeSize, int leafIndex, List<byte[]> auditPath, byte[] leafData) {
        byte[] computedRootHash = rootHashFromAuditPath(treeSize, leafIndex, new ArrayList<>(auditPath), leafData, Util.sha256Instance());
        return Arrays.equals(computedRootHash, expectedRootHash);
    }

    private static byte[] rootHashFromAuditPath(int treeSize, int leafIndex, List<byte[]> auditPath, byte[] leafData, MessageDigest digest) {
        if (treeSize == 1) {
            if (!auditPath.isEmpty()) {
                throw new IllegalStateException("Should have an empty audit path for trees of size 1");
            }
            return Util.leafHash(leafData, digest);
        }
        int k = k(treeSize);
        byte[] nextHash = auditPath.remove(auditPath.size() - 1);
        if (leafIndex < k) {
            byte[] leftChild = rootHashFromAuditPath(k, leafIndex, auditPath, leafData, digest);
            return branchHash(leftChild, nextHash, digest);
        } else {
            byte[] rightChild = rootHashFromAuditPath(treeSize - k, leafIndex - k, auditPath, leafData, digest);
            return branchHash(nextHash, rightChild, digest);
        }
    }

    public static boolean isValidConsistencyProof(int low, byte[] oldRoot, int high, byte[] newRoot, List<byte[]> consistencyProof) {
        if (low == high) {
            return Arrays.equals(oldRoot, newRoot) && consistencyProof.isEmpty();
        }
        byte[] computedOldRoot = oldRootHashFromConsistencyProof(low, high, new ArrayList<>(consistencyProof), oldRoot);
        byte[] computedNewRoot = rootHashFromConsistencyProof(low, high, new ArrayList<>(consistencyProof), oldRoot);
        return Arrays.equals(oldRoot, computedOldRoot) && Arrays.equals(newRoot, computedNewRoot);
    }

    private static byte[] rootHashFromConsistencyProof(int low, int high, List<byte[]> consistencyProof, byte[] oldRoot) {
        return rootHashFromConsistencyProof(0, high, low, consistencyProof, oldRoot, Util.sha256Instance(), true);
    }

    private static byte[] oldRootHashFromConsistencyProof(int low, int high, List<byte[]> consistencyProof, byte[] oldRoot) {
        return rootHashFromConsistencyProof(0, high, low, consistencyProof, oldRoot, Util.sha256Instance(), false);
    }

    private static byte[] rootHashFromConsistencyProof(int start, int end, int m, List<byte[]> consistencyProof, byte[] oldRoot, MessageDigest digest, boolean computeNewRoot) {
        int size = end - start;
        if (m == size) {
            if (start == 0) {
                // this is the b == true case in RFC 6962
                return oldRoot;
            }
            return consistencyProof.remove(consistencyProof.size() - 1);
        }
        int k = Util.k(size);
        int mid = start + k;
        byte[] nextHash = consistencyProof.remove(consistencyProof.size() - 1);
        if (m <= k) {
            byte[] leftChild = rootHashFromConsistencyProof(start, mid, m, consistencyProof, oldRoot, digest, computeNewRoot);
            if (computeNewRoot) {
                return Util.branchHash(leftChild, nextHash, digest);
            } else {
                return leftChild;
            }
        } else {
            byte[] rightChild = rootHashFromConsistencyProof(mid, end, m - k, consistencyProof, oldRoot, digest, computeNewRoot);
            return Util.branchHash(nextHash, rightChild, digest);
        }
    }
}
