package uk.gov.verifiablelog.dao.memoization;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.quicktheories.quicktheories.QuickTheory.qt;
import static org.quicktheories.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.quicktheories.generators.SourceDSL.strings;

import static uk.gov.verifiablelog.TestUtil.*;

public class InMemoryTests {
    @Test
    public void should_storeAllHashes() throws Exception {
        MemoizationStore inMemory = new InMemory();

        qt().forAll(strings().numericBetween(10,99), integers().between(0, 999), integers().between(1, 1000))
                .assuming((hash, leafIndex, size) -> leafIndex < size)
                .checkAssert((hash, leafIndex, size) -> {
                    inMemory.put(leafIndex, size, stringToBytes(hash));
                    assertThat(bytesToString(inMemory.get(leafIndex, size)), is(hash));
                });
    }
}
