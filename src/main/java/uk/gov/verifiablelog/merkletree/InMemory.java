package uk.gov.verifiablelog.merkletree;
import java.util.TreeMap;

public class InMemory implements MemoizationStore {

    private TreeMap<Integer, TreeMap<Integer, byte[]>> data;

    public InMemory() {
        data = new TreeMap<>();
    }

    @Override
    public void put(Integer start, Integer size, byte[] value) {
        TreeMap<Integer, byte[]> sizeBucket = data.get(size);

        if (sizeBucket == null) {
            sizeBucket = new TreeMap<>();
            data.put(size, sizeBucket);
        }
        sizeBucket.put(start, value);
    }

    @Override
    public byte[] get(Integer start, Integer size) {
        TreeMap<Integer, byte[]> sizeBucket = data.get(size);

        if (sizeBucket == null) {
            return null;
        }

        return sizeBucket.get(start);
    }
}





