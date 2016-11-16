package uk.gov.verifiablelog;

import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static uk.gov.verifiablelog.TestUtil.*;

public class VerifiableLogMockTests {
    private static final String emptyRootHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Test()
    public void getCurrentRootHash_usesStoreToRetrieveAndSave_for_emptyTreeAndEmptyStore() {
        MemoizationStore storeMock = Mockito.mock(MemoizationStore.class);
        VerifiableLog verifiableLog = makeVerifiableLog(Collections.emptyList(), storeMock);

        byte[] rootHash = verifiableLog.getCurrentRootHash();

        verify(storeMock, times(1)).get(0, 0);
        verify(storeMock, times(1)).put(0, 0, stringToBytes(emptyRootHash));
        assertThat(bytesToString(rootHash), is(emptyRootHash));
    }

    @Test
    public void getCurrentRootHash_usesStoreToRetrieveAndSave_for_treeWithLeaves() {
        List<byte[]> leafValues = Arrays.asList(
                stringToBytes("01"),
                stringToBytes("11"),
                stringToBytes("21"),
                stringToBytes("31")
        );
        MemoizationStore storeMock = Mockito.mock(MemoizationStore.class);

        VerifiableLog verifiableLog = makeVerifiableLog(leafValues, storeMock);

        byte[] rootHash = verifiableLog.getCurrentRootHash();

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
    public void getCurrentRootHash_retrievesAndUsesMemoizationStoreHashes() {
        List<byte[]> leafValues = Arrays.asList(
                stringToBytes("01"),
                stringToBytes("11"),
                stringToBytes("21"),
                stringToBytes("31")
        );
        byte[] expectedRootHash = stringToBytes("04");
        MemoizationStore storeMock = Mockito.mock(MemoizationStore.class);
        when(storeMock.get(0, 4)).thenReturn(expectedRootHash);

        VerifiableLog verifiableLog = makeVerifiableLog(leafValues, storeMock);

        byte[] rootHash = verifiableLog.getCurrentRootHash();

        verify(storeMock, times(1)).get(anyInt(), anyInt());
        verify(storeMock, times(1)).get(0, 4);
        verify(storeMock, never()).put(anyInt(), anyInt(), any(byte[].class));
        assertThat(bytesToString(rootHash), is(bytesToString(expectedRootHash)));
    }

    @Test
    public void auditProof_retrievesAndUsesMemoizationStoreHashes() {
        /**
         *  Tree and memoization expectations for an audit proof target node: 4, 8
         *
         *  ==  target node
         *  M - memoized before the test
         *  G - get call on MemoizationStore expected
         *  P - put call on MemoizationStore expected
         *  * - auditProof result node
         *
         *
         *                     08
         *                  /      \
         *              /              \
         *          /                      \
         *     *04(M,G)                    44
         *       /  \                    /    \
         *      /    \                /          \
         *     /      \            /                \
         *   02        22         42              *62(G,P)
         *  /  \      /  \      /    \            /       \
         * 01  11    21  31    41   *51(M,G)   61(G,P)  71(G,P)
         *                     ==
         */

        List<byte[]> leafValues = Arrays.asList(
                stringToBytes("01"),
                stringToBytes("11"),
                stringToBytes("21"),
                stringToBytes("31"),
                stringToBytes("41"),
                stringToBytes("51"),
                stringToBytes("61"),
                stringToBytes("71")
        );

        byte[] expectedNodeHash04 = stringToBytes("04");
        byte[] expectedNodeHash51 = stringToBytes("51");

        MemoizationStore storeMock = Mockito.mock(MemoizationStore.class);
        when(storeMock.get(0, 4)).thenReturn(expectedNodeHash04);
        when(storeMock.get(5, 1)).thenReturn(expectedNodeHash51);

        VerifiableLog verifiableLog = makeVerifiableLog(leafValues, storeMock);

        List<byte[]> auditProof = verifiableLog.auditProof(4, 8);

        verify(storeMock, times(5)).get(anyInt(), anyInt());
        verify(storeMock, times(1)).get(5, 1);
        verify(storeMock, times(1)).get(6, 1);
        verify(storeMock, times(1)).get(7, 1);
        verify(storeMock, times(1)).get(6, 2);
        verify(storeMock, times(1)).get(0, 4);

        verify(storeMock, times(3)).put(anyInt(), anyInt(), any(byte[].class));
        verify(storeMock, times(1)).put(eq(6), eq(1), any(byte[].class));
        verify(storeMock, times(1)).put(eq(7), eq(1), any(byte[].class));
        verify(storeMock, times(1)).put(eq(6), eq(1), any(byte[].class));

        assertThat(auditProof, hasSize(3));
        assertThat(auditProof.get(0), is(expectedNodeHash51));
        assertThat(auditProof.get(2), is(expectedNodeHash04));
    }

    @Test
    public void consistencyProof_retrievesAndUsesMemoizationStoreHashes() {
        /**
         *  Tree and memoization expectations for a consistency proof between trees of sizes 4 and 8
         *
         *  ==  target node
         *  M - memoized before the test
         *  G - get call on MemoizationStore expected
         *  P - put call on MemoizationStore expected
         *  * - consistencyProof result node
         *
         *
         *                     08
         *                  /      \
         *              /              \
         *          /                      \
         *        04                      *44(G,P)
         *       /  \                    /    \
         *      /    \                /          \
         *     /      \            /                \
         *   02        22       42(M,G)            62(M,G)
         *  /  \      /  \      /    \            /       \
         * 01  11    21  31    41    51          61       71
         *               ==
         */

        List<byte[]> leafValues = Arrays.asList(
                stringToBytes("01"),
                stringToBytes("11"),
                stringToBytes("21"),
                stringToBytes("31"),
                stringToBytes("41"),
                stringToBytes("51"),
                stringToBytes("61"),
                stringToBytes("71")
        );

        byte[] expectedNodeHash42 = stringToBytes("04");
        byte[] expectedNodeHash62 = stringToBytes("51");

        MemoizationStore storeMock = Mockito.mock(MemoizationStore.class);
        when(storeMock.get(4, 2)).thenReturn(expectedNodeHash42);
        when(storeMock.get(6, 2)).thenReturn(expectedNodeHash62);

        VerifiableLog verifiableLog = makeVerifiableLog(leafValues, storeMock);

        List<byte[]> consistencyProof = verifiableLog.consistencyProof(4, 8);

        verify(storeMock, times(3)).get(anyInt(), anyInt());
        verify(storeMock, times(1)).get(4, 4);
        verify(storeMock, times(1)).get(4, 2);
        verify(storeMock, times(1)).get(6, 2);

        verify(storeMock, times(1)).put(anyInt(), anyInt(), any(byte[].class));
        verify(storeMock, times(1)).put(eq(4), eq(4), any(byte[].class));

        assertThat(consistencyProof, hasSize(1));
    }

        private void verifyStoreCalledToGetAndPut(MemoizationStore storeMock, Integer start, Integer size) {
        verify(storeMock, times(1)).get(eq(start), eq(size));
        verify(storeMock, times(1)).put(eq(start), eq(size), any(byte[].class));
    }
}
