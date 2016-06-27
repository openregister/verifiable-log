package uk.gov.verifiablelog.merkletree;

/**
 * Defines the contract through which a consumer of a {@link MemoizationStore} can query and update a set of known
 * Merkle Tree root hashes for intermediate subtrees of a particular size and start index.
 */
public interface MemoizationStore {
    /**
     * Adds the root hash of a subtree to the set of known intermediate Merkle Tree root hashes.
     * @param start The zero-based index of the first leaf in the subtree
     * @param size The number of leaves in the subtree
     * @param value The Merkle Tree root hash of the subtree
     */
    void put(Integer start, Integer size, byte[] value);

    /**
     * Retrieves the root hash of a subtree from the set of known intermediate Merkle Tree root hashes.
     * @param start The zero-based index of the first leaf in the subtree
     * @param size The number of leaves in the subtree
     * @return The Merkle Tree root hash of the subtree if known, else null if not known
     */
    byte[] get(Integer start, Integer size);
}

