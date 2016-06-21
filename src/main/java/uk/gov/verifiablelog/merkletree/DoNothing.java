package uk.gov.verifiablelog.merkletree;

public class DoNothing implements MemoizationStore{

    @Override
    public void put(Integer start, Integer size, byte[] value) {

    }

    @Override
    public byte[] get(Integer start, Integer size) {
        return null;
    }
}

