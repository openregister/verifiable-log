package uk.gov.verifiablelog.merkletree;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MerkleUtilTests {

    @Test(expected = IllegalArgumentException.class)
    public void testKReturnsErrorWhenNotGreaterThanOne() {
        MerkleUtil.k(1);
    }

    @Test
    public void testKIsCalculatedCorrectly() {
        assertThat(MerkleUtil.k(2), is(1));
        assertThat(MerkleUtil.k(3), is(2));
        assertThat(MerkleUtil.k(4), is(2));
        assertThat(MerkleUtil.k(5), is(4));
        assertThat(MerkleUtil.k(8), is(4));
        assertThat(MerkleUtil.k(9), is(8));
        assertThat(MerkleUtil.k(35009563), is(33554432));
    }
}
