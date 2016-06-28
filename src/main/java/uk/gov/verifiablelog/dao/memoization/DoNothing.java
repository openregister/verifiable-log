package uk.gov.verifiablelog.dao.memoization;

/**
 * A dummy {@link MemoizationStore} that does not store Merkle Tree root hashes for intermediate subtrees of any size.
 */
public class DoNothing implements MemoizationStore{

    /***
     * Does not store the root hash for any intermediate Merkle Tree subtree.
     * @param start The zero-based index of the first leaf in the subtree
     * @param size The number of leaves in the subtree
     * @param value The Merkle Tree root hash of the subtree
     */
    @Override
    public void put(Integer start, Integer size, byte[] value) {

    }

    /**
     * Always returns null, i.e. nothing stored.
     * @param start The zero-based index of the first leaf in the subtree
     * @param size The number of leaves in the subtree
     * @return null
     */
    @Override
    public byte[] get(Integer start, Integer size) {
        return null;
    }
}

