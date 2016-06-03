package uk.gov.verifiablelog.merkletree;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.verifiablelog.merkletree.Util.branchHash;
import static uk.gov.verifiablelog.merkletree.Util.k;

public class MerkleTreeVerification {
    static boolean isValidAuditProof(byte[] rootHash, int treeSize, int leafIndex, List<byte[]> auditPath, byte[] leafData) {
        byte[] computedRootHash = rootHashFromAuditPath(treeSize, leafIndex, new ArrayList<>(auditPath), leafData, Util.sha256Instance());
        return Arrays.equals(computedRootHash, rootHash);
    }

    private static byte[] rootHashFromAuditPath(int treeSize, int leafIndex, List<byte[]> auditPath, byte[] leafData, MessageDigest digest) {
        if (treeSize == 1) {
            assert auditPath.isEmpty();
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

}
