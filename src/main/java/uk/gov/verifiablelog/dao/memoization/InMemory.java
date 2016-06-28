package uk.gov.verifiablelog.dao.memoization;

import java.util.TreeMap;

/**
 * A {@link MemoizationStore} that stores Merkle Tree root hashes in memory for intermediate subtrees of any size.
 */
public class InMemory implements MemoizationStore {

    private TreeMap<Integer, TreeMap<Integer, byte[]>> data;

    /**
     * Creates a new instance of an {@link InMemory} object.
     */
    public InMemory() {
        data = new TreeMap<>();
    }

    /**
     * Adds the root hash of a subtree to the set of known intermediate Merkle Tree root hashes stored in memory.
     * @param start The zero-based index of the first leaf in the subtree
     * @param size The number of leaves in the subtree
     * @param value The Merkle Tree root hash of the subtree
     */
    @Override
    public void put(Integer start, Integer size, byte[] value) {
        TreeMap<Integer, byte[]> sizeBucket = data.get(size);

        if (sizeBucket == null) {
            sizeBucket = new TreeMap<>();
            data.put(size, sizeBucket);
        }
        sizeBucket.put(start, value);
    }

    /**
     * Retrieves the root hash of a subtree from the set of known intermediate Merkle Tree root hashes
     * if it exists in the in-memory store.
     * @param start The zero-based index of the first leaf in the subtree
     * @param size The number of leaves in the subtree
     * @return The Merkle Tree root hash of the subtree if it exists in the in-memory store, else null.
     */
    @Override
    public byte[] get(Integer start, Integer size) {
        TreeMap<Integer, byte[]> sizeBucket = data.get(size);

        if (sizeBucket == null) {
            return null;
        }

        return sizeBucket.get(start);
    }
}





