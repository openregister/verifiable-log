package uk.gov.verifiablelog.store.memoization;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static uk.gov.verifiablelog.TestUtil.bytesToString;
import static uk.gov.verifiablelog.TestUtil.stringToBytes;

public class InMemoryPowOfTwoNoLeavesTests {
    @Test
    public void should_storeHashesForPowerOfTwoSubrees() {
        InMemoryPowOfTwoNoLeaves powOfTwoStore = new InMemoryPowOfTwoNoLeaves();
        // Test tree
        //                     (0,7)
        //          0,4                     (4,3)
        //    0,2         2,2         4,2
        // 0,1   1,1   2,1   3,1   4,1   5,1        6,1

        powOfTwoStore.put(0, 1, stringToBytes("01"));
        powOfTwoStore.put(1, 1, stringToBytes("11"));
        powOfTwoStore.put(2, 1, stringToBytes("21"));
        powOfTwoStore.put(3, 1, stringToBytes("31"));
        powOfTwoStore.put(4, 1, stringToBytes("41"));
        powOfTwoStore.put(5, 1, stringToBytes("51"));
        powOfTwoStore.put(6, 1, stringToBytes("61"));

        powOfTwoStore.put(0, 2, stringToBytes("02"));
        powOfTwoStore.put(2, 2, stringToBytes("22"));
        powOfTwoStore.put(4, 2, stringToBytes("42"));

        powOfTwoStore.put(0, 4, stringToBytes("04"));
        powOfTwoStore.put(4, 3, stringToBytes("43"));

        powOfTwoStore.put(0, 7, stringToBytes("07"));

        assertNull(powOfTwoStore.get(0, 1));
        assertNull(powOfTwoStore.get(1, 1));
        assertNull(powOfTwoStore.get(2, 1));
        assertNull(powOfTwoStore.get(3, 1));
        assertNull(powOfTwoStore.get(4, 1));
        assertNull(powOfTwoStore.get(5, 1));
        assertNull(powOfTwoStore.get(6, 1));

        assertThat(bytesToString(powOfTwoStore.get(0, 2)), is("02"));
        assertThat(bytesToString(powOfTwoStore.get(2, 2)), is("22"));
        assertThat(bytesToString(powOfTwoStore.get(4, 2)), is("42"));

        assertThat(bytesToString(powOfTwoStore.get(0, 4)), is("04"));
        assertThat(powOfTwoStore.get(4, 3), is(nullValue()));

        assertThat(powOfTwoStore.get(0, 7), is(nullValue()));
    }

}
