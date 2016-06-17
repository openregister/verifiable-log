package uk.gov.verifiablelog.merkletree;

public interface MemoizationStore {
    public void put(Integer start, Integer size, byte[] value);
    public byte[] get(Integer start, Integer size);
}

