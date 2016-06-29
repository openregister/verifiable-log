package uk.gov.verifiablelog;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.verifiablelog.Util.branchHash;
import static uk.gov.verifiablelog.Util.k;

/**
 * Verifies proofs provided by a Verifiable Log as implemented by {@link VerifiableLog}
 */
public class VerifiableLogVerification {

    /**
     * Verifies a piece of leaf data against an audit proof from a Verifiable Log.
     * @param expectedRootHash The Merkle Tree root hash of the Verifiable Log that computed the audit proof
     * @param treeSize The number of leaves in the Verifiable Log that computed the audit proof
     * @param leafIndex The zero-based index of the leaf for which the audit proof was computed
     * @param auditProof The audit proof to verify against
     * @param leafData The raw leaf data to verify
     * @return true if the leaf data can be verified against the audit proof, otherwise false
     */
    public static boolean isValidAuditProof(byte[] expectedRootHash, int treeSize, int leafIndex, List<byte[]> auditProof, byte[] leafData) {
        byte[] computedRootHash = rootHashFromAuditProof(treeSize, leafIndex, new ArrayList<>(auditProof), leafData, Util.sha256Instance());
        return Arrays.equals(computedRootHash, expectedRootHash);
    }

    /**
     * Verifies a the consistency of two tree sizes using a consistency proof from a Verifiable Log.
     * @param treeSize1 The number of leaves in the smaller Verifiable Log
     * @param oldRoot The Merkle Tree root hash of the smaller Verifiable Log
     * @param treeSize2 The number of leaves in the larger Verifiable Log
     * @param newRoot The Merkle Tree root hash of the larger Verifiable Log
     * @param consistencyProof The consistency proof to verify against
     * @return true if the root hashes for the two tree sizes can be verified as consistent, otherwise false
     */
    public static boolean isValidConsistencyProof(int treeSize1, byte[] oldRoot, int treeSize2, byte[] newRoot, List<byte[]> consistencyProof) {
        if (treeSize1 == treeSize2) {
            return Arrays.equals(oldRoot, newRoot) && consistencyProof.isEmpty();
        }
        byte[] computedOldRoot = oldRootHashFromConsistencyProof(treeSize1, treeSize2, new ArrayList<>(consistencyProof), oldRoot);
        byte[] computedNewRoot = newRootHashFromConsistencyProof(treeSize1, treeSize2, new ArrayList<>(consistencyProof), oldRoot);
        return Arrays.equals(oldRoot, computedOldRoot) && Arrays.equals(newRoot, computedNewRoot);
    }

    private static byte[] rootHashFromAuditProof(int treeSize, int leafIndex, List<byte[]> auditProof, byte[] leafData, MessageDigest digest) {
        if (treeSize == 1) {
            if (!auditProof.isEmpty()) {
                throw new IllegalStateException("Should have an empty audit path for trees of size 1");
            }
            return Util.leafHash(leafData, digest);
        }
        int k = k(treeSize);
        byte[] nextHash = auditProof.remove(auditProof.size() - 1);
        if (leafIndex < k) {
            byte[] leftChild = rootHashFromAuditProof(k, leafIndex, auditProof, leafData, digest);
            return branchHash(leftChild, nextHash, digest);
        } else {
            byte[] rightChild = rootHashFromAuditProof(treeSize - k, leafIndex - k, auditProof, leafData, digest);
            return branchHash(nextHash, rightChild, digest);
        }
    }

    private static byte[] newRootHashFromConsistencyProof(int low, int high, List<byte[]> consistencyProof, byte[] oldRoot) {
        return rootHashFromConsistencyProof(low, high, consistencyProof, oldRoot, Util.sha256Instance(), true, true);
    }

    private static byte[] oldRootHashFromConsistencyProof(int low, int high, List<byte[]> consistencyProof, byte[] oldRoot) {
        return rootHashFromConsistencyProof(low, high, consistencyProof, oldRoot, Util.sha256Instance(), false, true);
    }

    private static byte[] rootHashFromConsistencyProof(int low, int high, List<byte[]> consistencyProof, byte[] oldRoot, MessageDigest digest, boolean computeNewRoot, boolean startFromOldRoot) {
        if (low == high) {
            if (startFromOldRoot) {
                // this is the b == true case in RFC 6962
                return oldRoot;
            }
            return consistencyProof.remove(consistencyProof.size() - 1);
        }
        int k = Util.k(high);
        byte[] nextHash = consistencyProof.remove(consistencyProof.size() - 1);
        if (low <= k) {
            byte[] leftChild = rootHashFromConsistencyProof(low, k, consistencyProof, oldRoot, digest, computeNewRoot, startFromOldRoot);
            if (computeNewRoot) {
                return Util.branchHash(leftChild, nextHash, digest);
            } else {
                return leftChild;
            }
        } else {
            byte[] rightChild = rootHashFromConsistencyProof(low - k, high - k, consistencyProof, oldRoot, digest, computeNewRoot, false);
            return Util.branchHash(nextHash, rightChild, digest);
        }
    }
}
