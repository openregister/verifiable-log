package uk.gov.verifiablelog.merkletree;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static uk.gov.verifiablelog.merkletree.TestUtil.*;

public class MerkleTreeMockTests {
    private static final String emptyRootHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Test()
    public void currentRoot_should_useStoreToRetrieveAndSave_for_emptyTreeAndEmptyStore() {
        MemoizationStore storeMock = Mockito.mock(MemoizationStore.class);
        MerkleTree merkleTree = makeMerkleTree(Collections.emptyList(), storeMock);

        byte[] rootHash = merkleTree.currentRoot();

        verify(storeMock, times(1)).get(0, 0);
        verify(storeMock, times(1)).put(0, 0, stringToBytes(emptyRootHash));
        assertThat(bytesToString(rootHash), is(emptyRootHash));
    }

    @Test
    public void currentRoot_should_useStoreToRetrieveAndSave_for_treeWithLeaves() {
        List<byte[]> leafValues = Arrays.asList(
                stringToBytes("0a"),
                stringToBytes("0b"),
                stringToBytes("0c"),
                stringToBytes("0d")
        );
        MemoizationStore storeMock = Mockito.mock(MemoizationStore.class);
        MerkleTree merkleTree = makeMerkleTree(leafValues, storeMock);

        byte[] rootHash = merkleTree.currentRoot();

        verify(storeMock, times(7)).get(anyInt(), anyInt());
        verify(storeMock, times(7)).put(anyInt(), anyInt(), any(byte[].class));

        verifyStoreCalledToGetAndPut(storeMock, 0, 1);
        verifyStoreCalledToGetAndPut(storeMock, 1, 1);
        verifyStoreCalledToGetAndPut(storeMock, 2, 1);
        verifyStoreCalledToGetAndPut(storeMock, 3, 1);
        verifyStoreCalledToGetAndPut(storeMock, 0, 2);
        verifyStoreCalledToGetAndPut(storeMock, 2, 2);

        verify(storeMock, times(1)).get(0, 4);
        verify(storeMock, times(1)).put(0, 4, rootHash);
    }

    @Test
    public void currentRoot_should_retrieveAndUseMemoizationStoreHashes() {
        List<byte[]> leafValues = Arrays.asList(
                stringToBytes("0a"),
                stringToBytes("0b"),
                stringToBytes("0c"),
                stringToBytes("0d")
        );
        byte[] expectedRootHash = stringToBytes("04");
        MemoizationStore storeMock = Mockito.mock(MemoizationStore.class);
        when(storeMock.get(0, 4)).thenReturn(expectedRootHash);

        MerkleTree merkleTree = makeMerkleTree(leafValues, storeMock);

        byte[] rootHash = merkleTree.currentRoot();

        verify(storeMock, times(1)).get(anyInt(), anyInt());
        verify(storeMock, times(1)).get(0, 4);
        verify(storeMock, never()).put(anyInt(), anyInt(), any(byte[].class));
        assertThat(bytesToString(rootHash), is(bytesToString(expectedRootHash)));
    }

    private void verifyStoreCalledToGetAndPut(MemoizationStore storeMock, Integer start, Integer size) {
        verify(storeMock, times(1)).get(eq(start), eq(size));
        verify(storeMock, times(1)).put(eq(start), eq(size), any(byte[].class));
    }
}
