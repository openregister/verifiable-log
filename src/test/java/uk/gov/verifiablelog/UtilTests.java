package uk.gov.verifiablelog;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UtilTests {

    @Test(expected = IllegalArgumentException.class)
    public void testKReturnsErrorWhenNotGreaterThanOne() {
        Util.k(1);
    }

    @Test
    public void testKIsCalculatedCorrectly() {
        assertThat(Util.k(2), is(1));
        assertThat(Util.k(3), is(2));
        assertThat(Util.k(4), is(2));
        assertThat(Util.k(5), is(4));
        assertThat(Util.k(8), is(4));
        assertThat(Util.k(9), is(8));
        assertThat(Util.k(35009563), is(33554432));
    }
}
