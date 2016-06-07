package uk.gov.verifiablelog.merkletree;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.verifiablelog.merkletree.Util.branchHash;
import static uk.gov.verifiablelog.merkletree.Util.k;
import static uk.gov.verifiablelog.merkletree.Util.sha256Instance;

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
            return Arrays.equals(oldRoot, newRoot);
        }
        byte[] computedOldRoot = oldRootHashFromConsistencyProof(low, consistencyProof, oldRoot);
        byte[] computedNewRoot = newRootHashFromConsistencyProof(high, consistencyProof, oldRoot);
        return Arrays.equals(oldRoot, computedOldRoot) && Arrays.equals(newRoot, computedNewRoot);
    }

    private static byte[] newRootHashFromConsistencyProof(int high, List<byte[]> consistencyProof, byte[] oldRoot) {
        return branchHash(oldRoot, consistencyProof.get(0), sha256Instance());
    }

    private static byte[] oldRootHashFromConsistencyProof(int low, List<byte[]> consistencyProof, byte[] oldRoot) {
        return oldRoot;
    }
}
