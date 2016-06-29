package uk.gov.verifiablelog.store;

/**
 * Defines the contract through which a consumer of a {@link MerkleLeafStore} can query for leaf data.
 */
public interface MerkleLeafStore {

    /**
     * Gets a leaf value by its index.
     * @param leafIndex The zero-based index of the leaf, which must be less than {@link #totalLeaves()}
     * @return The raw data for the leaf
     */
    byte[] getLeafValue(int leafIndex);

    /**
     * Gets the total number of leaves.
     * @return The total number of leaves
     */
    int totalLeaves();
}
