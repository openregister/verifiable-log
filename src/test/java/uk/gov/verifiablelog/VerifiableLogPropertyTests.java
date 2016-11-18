package uk.gov.verifiablelog;

import org.junit.Test;
import uk.gov.verifiablelog.store.memoization.InMemory;
import uk.gov.verifiablelog.store.memoization.InMemoryPowOfTwo;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.quicktheories.quicktheories.QuickTheory.qt;
import static org.quicktheories.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.quicktheories.generators.SourceDSL.lists;
import static org.quicktheories.quicktheories.generators.SourceDSL.strings;

import static uk.gov.verifiablelog.TestUtil.*;

public class VerifiableLogPropertyTests {
    @Test
    public void property_rootHashFromMemoizedLogIsSameAsRootHashFromNonMemoizedLog() {
        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(1, 1000))
                .checkAssert(entryStrings -> {
                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());

                    MemoizationStore inMemoryMs = new InMemory();
                    VerifiableLog memoizedLog = makeVerifiableLog(entries, inMemoryMs);
                    memoizedLog.getCurrentRootHash();

                    MemoizationStore inMemoryPowOfTwoMs = new InMemoryPowOfTwo();
                    VerifiableLog memoizedPowOfTwoLog = makeVerifiableLog(entries, inMemoryPowOfTwoMs);
                    memoizedPowOfTwoLog.getCurrentRootHash();

                    VerifiableLog nonMemoizedLog = makeVerifiableLog(entries);

                    assertThat(bytesToString(memoizedLog.getCurrentRootHash()), is(bytesToString(nonMemoizedLog.getCurrentRootHash())));
                    assertThat(bytesToString(memoizedPowOfTwoLog.getCurrentRootHash()), is(bytesToString(nonMemoizedLog.getCurrentRootHash())));
                });
    }

    @Test
    public void property_canConstructRootHashFromLeafAndAuditProof() throws Exception {
        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(1, 1000), integers().between(0, 999))
                .assuming((entries, leafIndex) -> leafIndex < entries.size())
                .checkAssert((entryStrings, leafIndex) -> {
                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());
                    VerifiableLog verifiableLog = makeVerifiableLog(entries);
                    List<byte[]> auditProof = verifiableLog.auditProof(leafIndex, entries.size());
                    assertThat(VerifiableLogVerification.isValidAuditProof(verifiableLog.getCurrentRootHash(), entries.size(), leafIndex, auditProof, entries.get(leafIndex)), is(true));
                });
    }

    @Test
    public void property_auditProofFromMemoizedLogIsSameAsAuditProofFromNonMemoizedLog() {
        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(1, 1000), integers().between(0, 999))
                .assuming((entries, leafIndex) -> leafIndex < entries.size())
                .checkAssert((entryStrings, leafIndex) -> {
                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());

                    MemoizationStore inMemoryMs = new InMemory();
                    VerifiableLog memoizedLog = makeVerifiableLog(entries, inMemoryMs);
                    memoizedLog.getCurrentRootHash();

                    MemoizationStore inMemoryPowOfTwoMs = new InMemoryPowOfTwo();
                    VerifiableLog memoizedPowOfTwoLog = makeVerifiableLog(entries, inMemoryPowOfTwoMs);
                    memoizedPowOfTwoLog.getCurrentRootHash();

                    VerifiableLog nonMemoizedLog = makeVerifiableLog(entries);

                    List<byte[]> auditPathNonMemoized = nonMemoizedLog.auditProof(leafIndex, entries.size());
                    List<byte[]> auditPathInMemory = memoizedLog.auditProof(leafIndex, entries.size());
                    List<byte[]> auditPathInMemoryPowOfTwo = memoizedPowOfTwoLog.auditProof(leafIndex, entries.size());

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
                    VerifiableLog verifiableLog = makeVerifiableLog(entries);
                    byte[] lowRoot = makeVerifiableLog(entries.subList(0, low)).getCurrentRootHash();
                    byte[] highRoot = makeVerifiableLog(entries.subList(0, high)).getCurrentRootHash();
                    List<byte[]> consistencyProof = verifiableLog.consistencyProof(low, high);

                    return VerifiableLogVerification.isValidConsistencyProof(low, lowRoot, high, highRoot, consistencyProof);
                });
    }

    @Test
    public void property_consistencyProofForMemoizedLogIsSameAsConsistencyProofForNonMemoizedLog() {
        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(2, 1000), integers().between(1, 1000), integers().between(1, 1000))
                .assuming((entries, low, high) -> low <= high && high <= entries.size())
                .checkAssert((entryStrings, low, high) -> {
                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());

                    MemoizationStore inMemoryMs = new InMemory();
                    VerifiableLog memoizedLog = makeVerifiableLog(entries, inMemoryMs);
                    memoizedLog.getCurrentRootHash();

                    MemoizationStore inMemoryPowOfTwoMs = new InMemoryPowOfTwo();
                    VerifiableLog memoizedPowOfTwoLog = makeVerifiableLog(entries, inMemoryPowOfTwoMs);
                    memoizedPowOfTwoLog.getCurrentRootHash();

                    VerifiableLog nonMemoizedLog = makeVerifiableLog(entries);

                    List<byte[]> consistencyProofForNonMemoized = nonMemoizedLog.consistencyProof(low, high);
                    List<byte[]> consistencyProofForInMemory = memoizedLog.consistencyProof(low, high);
                    List<byte[]> consistencyProofForInMemoryPowOfTwo = memoizedPowOfTwoLog.consistencyProof(low, high);

                    assertThat(bytesToString(consistencyProofForInMemory), is(bytesToString(consistencyProofForNonMemoized)));
                    assertThat(bytesToString(consistencyProofForInMemoryPowOfTwo), is(bytesToString(consistencyProofForNonMemoized)));
                });
    }
}
