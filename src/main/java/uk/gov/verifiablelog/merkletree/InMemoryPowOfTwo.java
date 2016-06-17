package uk.gov.verifiablelog.merkletree;


import java.util.TreeMap;

public class InMemoryPowOfTwo implements MemoizationStore {

    private TreeMap<Integer, TreeMap<Integer, byte[]>> data;

    public InMemoryPowOfTwo() {
        data = new TreeMap<>();
    }


    @Override
    public void put(Integer start, Integer size, byte[] value) {
        if (((start == 0) || (Integer.bitCount(start) == 1)) &&
                ((size == 0) || (Integer.bitCount(size) == 1))) {

            TreeMap<Integer, byte[]> sizeBucket = data.get(size);

            if (sizeBucket == null) {
                sizeBucket = new TreeMap<>();
                data.put(size, sizeBucket);
            }
            sizeBucket.put(start, value);
        }
    }

    @Override
    public byte[] get(Integer start, Integer size) {
        TreeMap<Integer, byte[]> sizeBucket = data.get(size);

        return sizeBucket == null ? null : sizeBucket.get(start);
    }
}
