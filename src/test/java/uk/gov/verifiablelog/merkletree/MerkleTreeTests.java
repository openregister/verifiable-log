package uk.gov.verifiablelog.merkletree;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MerkleTreeTests {

    private MerkleTree merkleTree;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        merkleTree = new MerkleTree(MessageDigest.getInstance("SHA-256"));
    }

    @Test
    public void expectedRootFromEmptyMerkleTree() throws NoSuchAlgorithmException {
        String emptyRootHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        assertThat(bytesToString(merkleTree.currentRoot()), is(emptyRootHash));
    }

    @Test
    public void expectedRootFromMerkleTreeWithLeaves() throws NoSuchAlgorithmException {
        merkleTree.addLeaf(new byte[]{});
        assertThat(bytesToString(merkleTree.currentRoot()), is("6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d"));

        merkleTree.addLeaf(new byte[]{0x00});
        assertThat(bytesToString(merkleTree.currentRoot()), is("fac54203e7cc696cf0dfcb42c92a1d9dbaf70ad9e621f4bd8d98662f00e3c125"));

        merkleTree.addLeaf(new byte[]{0x10});
        assertThat(bytesToString(merkleTree.currentRoot()), is("aeb6bcfe274b70a14fb067a5e5578264db0fa9b51af5e0ba159158f329e06e77"));

        merkleTree.addLeaf(new byte[]{0x20, 0x21});
        assertThat(bytesToString(merkleTree.currentRoot()), is("d37ee418976dd95753c1c73862b9398fa2a2cf9b4ff0fdfe8b30cd95209614b7"));

        merkleTree.addLeaf(new byte[]{0x30, 0x31});
        assertThat(bytesToString(merkleTree.currentRoot()), is("4e3bbb1f7b478dcfe71fb631631519a3bca12c9aefca1612bfce4c13a86264d4"));

        merkleTree.addLeaf(new byte[]{0x40, 0x41, 0x42, 0x43});
        assertThat(bytesToString(merkleTree.currentRoot()), is("76e67dadbcdf1e10e1b74ddc608abd2f98dfb16fbce75277b5232a127f2087ef"));

        merkleTree.addLeaf(new byte[]{0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57});
        assertThat(bytesToString(merkleTree.currentRoot()), is("ddb89be403809e325750d3d263cd78929c2942b7942a34b77e122c9594a74c8c"));

        merkleTree.addLeaf(new byte[]{0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f});
        assertThat(bytesToString(merkleTree.currentRoot()), is("5dc9da79a70659a9ad559cb701ded9a2ab9d823aad2f4960cfe370eff4604328"));
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
}
