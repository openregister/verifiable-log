package uk.gov.verifiablelog.merkletree;

import org.junit.Test;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.quicktheories.quicktheories.QuickTheory.qt;
import static org.quicktheories.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.quicktheories.generators.SourceDSL.lists;
import static org.quicktheories.quicktheories.generators.SourceDSL.strings;

import static uk.gov.verifiablelog.merkletree.TestUtil.*;

public class MerkleTreePropertyTests {
    @Test
    public void property_rootHashFromMemoizedTreeIsSameAsRootHashFromNonMemoizedTree() {
        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(1, 1000))
                .checkAssert(entryStrings -> {
                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());

                    MemoizationStore inMemoryMs = new InMemory();
                    MerkleTree memoizedTree = makeMerkleTree(entries, inMemoryMs);
                    memoizedTree.currentRoot();

                    MemoizationStore inMemoryPowOfTwoMs = new InMemoryPowOfTwo();
                    MerkleTree memoizedPowOfTwoTree = makeMerkleTree(entries, inMemoryPowOfTwoMs);
                    memoizedPowOfTwoTree.currentRoot();

                    MerkleTree nonMemoizedTree = makeMerkleTree(entries);

                    assertThat(bytesToString(memoizedTree.currentRoot()), is(bytesToString(nonMemoizedTree.currentRoot())));
                    assertThat(bytesToString(memoizedPowOfTwoTree.currentRoot()), is(bytesToString(nonMemoizedTree.currentRoot())));
                });
    }

    @Test
    public void property_canConstructRootHashFromLeafAndAuditPath() throws Exception {
        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(1, 1000), integers().between(0, 999))
                .assuming((entries, leafIndex) -> leafIndex < entries.size())
                .checkAssert((entryStrings, leafIndex) -> {
                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());
                    MerkleTree merkleTree = makeMerkleTree(entries);
                    List<byte[]> auditPath = merkleTree.auditProof(leafIndex, entries.size());
                    assertThat(MerkleTreeVerification.isValidAuditProof(merkleTree.currentRoot(), entries.size(), leafIndex, auditPath, entries.get(leafIndex)), is(true));
                });
    }

    @Test
    public void property_auditPathFromMemoizedTreeIsSameAsAuditPathFromNonMemoizedTree() {
        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(1, 1000), integers().between(0, 999))
                .assuming((entries, leafIndex) -> leafIndex < entries.size())
                .checkAssert((entryStrings, leafIndex) -> {
                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());

                    MemoizationStore inMemoryMs = new InMemory();
                    MerkleTree memoizedTree = makeMerkleTree(entries, inMemoryMs);
                    memoizedTree.currentRoot();

                    MemoizationStore inMemoryPowOfTwoMs = new InMemoryPowOfTwo();
                    MerkleTree memoizedPowOfTwoTree = makeMerkleTree(entries, inMemoryPowOfTwoMs);
                    memoizedPowOfTwoTree.currentRoot();

                    MerkleTree nonMemoizedTree = makeMerkleTree(entries);

                    List<byte[]> auditPathNonMemoized = nonMemoizedTree.auditProof(leafIndex, entries.size());
                    List<byte[]> auditPathInMemory = memoizedTree.auditProof(leafIndex, entries.size());
                    List<byte[]> auditPathInMemoryPowOfTwo = memoizedPowOfTwoTree.auditProof(leafIndex, entries.size());

                    assertThat(bytesToString(auditPathInMemory), is(bytesToString(auditPathNonMemoized)));
                    assertThat(bytesToString(auditPathInMemoryPowOfTwo), is(bytesToString(auditPathNonMemoized)));
                });
    }

    @Test
    public void property_canVerifyConsistencyProof() {
        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(2, 1000), integers().between(1, 1000), integers().between(1, 1000))
                .assuming((entries, low, high) -> low <= high && high <= entries.size())
                .check((entryStrings, low, high) -> {
                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());
                    MerkleTree merkleTree = makeMerkleTree(entries);
                    byte[] lowRoot = makeMerkleTree(entries.subList(0, low)).currentRoot();
                    byte[] highRoot = makeMerkleTree(entries.subList(0, high)).currentRoot();
                    List<byte[]> consistencyProof = merkleTree.consistencyProof(low, high);

                    return MerkleTreeVerification.isValidConsistencyProof(low, lowRoot, high, highRoot, consistencyProof);
                });
    }

    @Test
    public void property_consistencyProofForMemoizedTreeIsSameAsConsistencyProofForNonMemoizedTree() {
        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(2, 1000), integers().between(1, 1000), integers().between(1, 1000))
                .assuming((entries, low, high) -> low <= high && high <= entries.size())
                .checkAssert((entryStrings, low, high) -> {
                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());

                    MemoizationStore inMemoryMs = new InMemory();
                    MerkleTree memoizedTree = makeMerkleTree(entries, inMemoryMs);
                    memoizedTree.currentRoot();

                    MemoizationStore inMemoryPowOfTwoMs = new InMemoryPowOfTwo();
                    MerkleTree memoizedPowOfTwoTree = makeMerkleTree(entries, inMemoryPowOfTwoMs);
                    memoizedPowOfTwoTree.currentRoot();

                    MerkleTree nonMemoizedTree = makeMerkleTree(entries);

                    List<byte[]> consistencyProofForNonMemoized = nonMemoizedTree.consistencyProof(low, high);
                    List<byte[]> consistencyProofForInMemory = memoizedTree.consistencyProof(low, high);
                    List<byte[]> consistencyProofForInMemoryPowOfTwo = memoizedPowOfTwoTree.consistencyProof(low, high);

                    assertThat(bytesToString(consistencyProofForInMemory), is(bytesToString(consistencyProofForNonMemoized)));
                    assertThat(bytesToString(consistencyProofForInMemoryPowOfTwo), is(bytesToString(consistencyProofForNonMemoized)));
                });
    }
}
