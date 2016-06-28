package uk.gov.verifiablelog;

import uk.gov.verifiablelog.dao.memoization.DoNothing;
import uk.gov.verifiablelog.dao.memoization.MemoizationStore;
import uk.gov.verifiablelog.dao.MerkleLeafDAO;
import uk.gov.verifiablelog.merkletree.MerkleUtil;

import java.security.MessageDigest;
import java.util.*;

/**
 * An implementation of a Verifiable Log.
 * @see <a href="https://tools.ietf.org/html/rfc6962">RFC 6962</a>
 */
public class VerifiableLog {

    private final MessageDigest messageDigest;
    private final MerkleLeafDAO merkleLeafDAO;
    private final MemoizationStore memoizationStore;

    /**
     * Creates a new instance of a {@link VerifiableLog} object that does not memoize Merkle Tree root hashes of
     * intermediate subtrees.
     * @param messageDigest The algorithm to use when creating hash values of leaf data and intermediate Merkle Tree nodes
     * @param merkleLeafDAO An object providing access to the raw leaf data
     */
    public VerifiableLog(MessageDigest messageDigest, MerkleLeafDAO merkleLeafDAO) {
        this(messageDigest, merkleLeafDAO, null);
    }

    /**
     * Creates a new instance of a {@link VerifiableLog} object that memoizes Merkle Tree root hashes of
     * intermediate subtrees using the a {@link MemoizationStore}.
     * @param messageDigest The algorithm to use when creating hash values of leaf data and intermediate Merkle Tree nodes
     * @param merkleLeafDAO An object providing access to the raw leaf data
     * @param memoizationStore The {@link MemoizationStore} to use when memoizing intermediate subtree root hashes
     */
    public VerifiableLog(MessageDigest messageDigest, MerkleLeafDAO merkleLeafDAO, MemoizationStore memoizationStore) {
        this.messageDigest = messageDigest;
        this.merkleLeafDAO = merkleLeafDAO;
        this.memoizationStore = memoizationStore == null ? new DoNothing(): memoizationStore;
    }

    /**
     * The root hash of the Verifiable Log created from all current leaf values ordered by index.
     * @return The Merkle Tree root hash
     */
    public byte[] currentRoot() {
        return subtreeHash(0, merkleLeafDAO.totalLeaves());
    }

    /**
     * The audit proof for the leaf value at a specified index of a Verifiable Log.
     * @param leafIndex The zero-based index of the leaf for which the audit proof is required
     * @param treeSize The number of leaves in the Verifiable Log for which the audit proof is required
     * @return The ordered list of Merkle Tree hashes that provide the audit proof for the specified leaf
     */
    public List<byte[]> auditProof(int leafIndex, int treeSize) {
        return subtreeAuditProof(leafIndex, 0, treeSize);
    }

    /**
     * The consistency proof of a Verifiable Log at two tree sizes.
     * @param treeSize1 The number of leaves in the smaller Verifiable Log
     * @param treeSize2 The number of leaves in the larger Verifiable Log
     * @return The ordered list of Merkle Tree hashes that provide the consistency proof between the specified tree sizes
     */
    public List<byte[]> consistencyProof(int treeSize1, int treeSize2) {
        if (treeSize1 <= 0) {
            // RFC 6962 §2.1.2 assumes `0 < m < n`; we assume `0 < m <= n`
            throw new IllegalArgumentException("treeSize1 must be strictly positive");
        }
        return subtreeConsistencyProof(treeSize1, treeSize2, 0, true);
    }

    private List<byte[]> subtreeConsistencyProof(int low, int high, int start, boolean startFromOldRoot) {
        if (low == high) {
            if (startFromOldRoot) {
                // this is the b == true case in RFC 6962
                return new ArrayList<>();
            }
            List<byte[]> consistencySet = new ArrayList<>();
            consistencySet.add(subtreeHash(start, high));
            return consistencySet;
        }
        int k = MerkleUtil.k(high);
        if (low <= k) {
            List<byte[]> subtreeConsistencySet = subtreeConsistencyProof(low, k, start, startFromOldRoot);
            subtreeConsistencySet.add(subtreeHash(start + k, high - k));
            return subtreeConsistencySet;
        } else {
            List<byte[]> subtreeConsistencySet = subtreeConsistencyProof(low - k, high - k, start + k, false);
            subtreeConsistencySet.add(subtreeHash(start, k));
            return subtreeConsistencySet;
        }
    }

    // audit path within subtree of leaves from start (inclusive) to end (exclusive)
    private List<byte[]> subtreeAuditProof(int leafIndex, int start, int snapshotSize) {
        if (snapshotSize <= 1) {
            return new ArrayList<>();
        }
        int k = MerkleUtil.k(snapshotSize);
        if (leafIndex < k) {
            List<byte[]> subtreePath = subtreeAuditProof(leafIndex, start, k);
            subtreePath.add(subtreeHash(start + k, snapshotSize - k));
            return subtreePath;
        } else {
            List<byte[]> subtreePath = subtreeAuditProof(leafIndex - k, start + k, snapshotSize - k);
            subtreePath.add(subtreeHash(start, k));
            return subtreePath;
        }
    }

    // hash of subtree of given size
    private byte[] computeSubtreeHash(int start, int size) {
        if (size == 0) {
            return emptyTreeHash();
        } else if (size == 1) {
            return MerkleUtil.leafHash(merkleLeafDAO.getLeafValue(start), messageDigest);
        } else {
            int k = MerkleUtil.k(size);
            byte[] leftSubtreeHash = subtreeHash(start, k);
            byte[] rightSubtreeHash = subtreeHash(k + start, size - k);
            return MerkleUtil.branchHash(leftSubtreeHash, rightSubtreeHash, messageDigest);
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


