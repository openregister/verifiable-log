package uk.gov.verifiablelog.merkletree;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static uk.gov.verifiablelog.merkletree.TestUtil.*;

@RunWith(Parameterized.class)
public class MerkleTreeTests {

    public static final List<byte[]> TEST_INPUTS = Arrays.asList(
            new byte[]{},
            new byte[]{0x00},
            new byte[]{0x10},
            new byte[]{0x20, 0x21},
            new byte[]{0x30, 0x31},
            new byte[]{0x40, 0x41, 0x42, 0x43},
            new byte[]{0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57},
            new byte[]{0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f});

    private static final String emptyRootHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private final Supplier<MemoizationStore> memoizationStoreSupplier;

    private List<byte[]> leafValues;
    private MerkleTree merkleTree;

    public MerkleTreeTests(Supplier<MemoizationStore> memoizationStoreSupplier) {
        this.memoizationStoreSupplier = memoizationStoreSupplier;
    }

    @Parameters(name = "case: {index}")
    public static Collection<Supplier<MemoizationStore>> data() {
        return Arrays.asList(
                InMemory::new,
                InMemoryPowOfTwo::new
        );
    }

    @Before
    public void beforeEach() throws NoSuchAlgorithmException {
        leafValues = new ArrayList();
        merkleTree = makeMerkleTree(leafValues, memoizationStoreSupplier.get());
    }

    @Test
    public void expectedRootFromEmptyMerkleTree() {
        assertThat(bytesToString(merkleTree.currentRoot()), is(emptyRootHash));
    }

    @Test
    public void expectedRootFromMerkleTreeWithLeavesForTree() {
        leafValues.add(TEST_INPUTS.get(0));
        assertThat(bytesToString(merkleTree.currentRoot()), is("6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d"));

        leafValues.add(TEST_INPUTS.get(1));
        assertThat(bytesToString(merkleTree.currentRoot()), is("fac54203e7cc696cf0dfcb42c92a1d9dbaf70ad9e621f4bd8d98662f00e3c125"));

        leafValues.add(TEST_INPUTS.get(2));
        assertThat(bytesToString(merkleTree.currentRoot()), is("aeb6bcfe274b70a14fb067a5e5578264db0fa9b51af5e0ba159158f329e06e77"));

        leafValues.add(TEST_INPUTS.get(3));
        assertThat(bytesToString(merkleTree.currentRoot()), is("d37ee418976dd95753c1c73862b9398fa2a2cf9b4ff0fdfe8b30cd95209614b7"));

        leafValues.add(TEST_INPUTS.get(4));
        assertThat(bytesToString(merkleTree.currentRoot()), is("4e3bbb1f7b478dcfe71fb631631519a3bca12c9aefca1612bfce4c13a86264d4"));

        leafValues.add(TEST_INPUTS.get(5));
        assertThat(bytesToString(merkleTree.currentRoot()), is("76e67dadbcdf1e10e1b74ddc608abd2f98dfb16fbce75277b5232a127f2087ef"));

        leafValues.add(TEST_INPUTS.get(6));
        assertThat(bytesToString(merkleTree.currentRoot()), is("ddb89be403809e325750d3d263cd78929c2942b7942a34b77e122c9594a74c8c"));

        leafValues.add(TEST_INPUTS.get(7));
        assertThat(bytesToString(merkleTree.currentRoot()), is("5dc9da79a70659a9ad559cb701ded9a2ab9d823aad2f4960cfe370eff4604328"));
    }

    @Test
    public void expectedAuditPathForSnapshotSize() {
        for (byte[] testInput : TEST_INPUTS) {
            leafValues.add(testInput);
        }

        List<byte[]> auditPath1 = merkleTree.pathToRootAtSnapshot(0, 0);
        assertThat(auditPath1.size(), is(0));

        List<byte[]> auditPath2 = merkleTree.pathToRootAtSnapshot(0, 1);
        assertThat(auditPath2.size(), is(0));

        List<byte[]> auditPath3 = merkleTree.pathToRootAtSnapshot(0, 8);
        assertThat(bytesToString(auditPath3), is(Arrays.asList(
                "96a296d224f285c67bee93c30f8a309157f0daa35dc5b87e410b78630a09cfc7",
                "5f083f0a1a33ca076a95279832580db3e0ef4584bdff1f54c8a360f50de3031e",
                "6b47aaf29ee3c2af9af889bc1fb9254dabd31177f16232dd6aab035ca39bf6e4")));

        List<byte[]> auditPath4 = merkleTree.pathToRootAtSnapshot(5, 8);
        assertThat(bytesToString(auditPath4), is(Arrays.asList(
                "bc1a0643b12e4d2d7c77918f44e0f4f79a838b6cf9ec5b5c283e1f4d88599e6b",
                "ca854ea128ed050b41b35ffc1b87b8eb2bde461e9e3b5596ece6b9d5975a0ae0",
                "d37ee418976dd95753c1c73862b9398fa2a2cf9b4ff0fdfe8b30cd95209614b7")));

        List<byte[]> auditPath5 = merkleTree.pathToRootAtSnapshot(2, 3);
        assertThat(bytesToString(auditPath5), is(Arrays.asList(
                "fac54203e7cc696cf0dfcb42c92a1d9dbaf70ad9e621f4bd8d98662f00e3c125")));

        List<byte[]> auditPath6 = merkleTree.pathToRootAtSnapshot(1, 5);
        assertThat(bytesToString(auditPath6), is(Arrays.asList(
                "6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d",
                "5f083f0a1a33ca076a95279832580db3e0ef4584bdff1f54c8a360f50de3031e",
                "bc1a0643b12e4d2d7c77918f44e0f4f79a838b6cf9ec5b5c283e1f4d88599e6b")));
    }

    @Test
    public void expectedConsistencySetForSnapshots() {
        for (byte[] testInput : TEST_INPUTS) {
            leafValues.add(testInput);
        }

        List<byte[]> consistencySet1 = merkleTree.snapshotConsistency(1, 1);
        assertThat(consistencySet1.size(), is(0));

        List<byte[]> consistencySet2 = merkleTree.snapshotConsistency(1, 8);
        assertThat(bytesToString(consistencySet2), is(Arrays.asList(
                "96a296d224f285c67bee93c30f8a309157f0daa35dc5b87e410b78630a09cfc7",
                "5f083f0a1a33ca076a95279832580db3e0ef4584bdff1f54c8a360f50de3031e",
                "6b47aaf29ee3c2af9af889bc1fb9254dabd31177f16232dd6aab035ca39bf6e4")));

        List<byte[]> consistencySet3 = merkleTree.snapshotConsistency(6, 8);
        assertThat(bytesToString(consistencySet3), is(Arrays.asList(
                "0ebc5d3437fbe2db158b9f126a1d118e308181031d0a949f8dededebc558ef6a",
                "ca854ea128ed050b41b35ffc1b87b8eb2bde461e9e3b5596ece6b9d5975a0ae0",
                "d37ee418976dd95753c1c73862b9398fa2a2cf9b4ff0fdfe8b30cd95209614b7")));

        List<byte[]> consistencySet4 = merkleTree.snapshotConsistency(2, 5);
        assertThat(bytesToString(consistencySet4), is(Arrays.asList(
                "5f083f0a1a33ca076a95279832580db3e0ef4584bdff1f54c8a360f50de3031e",
                "bc1a0643b12e4d2d7c77918f44e0f4f79a838b6cf9ec5b5c283e1f4d88599e6b")));
    }
}

