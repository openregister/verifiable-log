package uk.gov.verifiablelog.merkletree;

import org.junit.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import javax.xml.bind.DatatypeConverter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.quicktheories.quicktheories.QuickTheory.qt;
import static org.quicktheories.quicktheories.generators.SourceDSL.*;

import java.lang.reflect.Array;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

class ThingsToTestTogether {
    public MerkleTree theTree;
    public List<byte[]> theLeaves;
    public MemoizationStore theMemo;

    public ThingsToTestTogether(MerkleTree theTree, List<byte[]> theLeaves, MemoizationStore theMemo) {
        this.theTree = theTree;
        this.theLeaves = theLeaves;
        this.theMemo = theMemo;
    }
}

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


    private ArrayList<ThingsToTestTogether> thingsToTestTogether;

    @Before
    public void beforeEach() throws NoSuchAlgorithmException {
        MemoizationStore ms = new InMemory();
        MemoizationStore ms2 = new InMemoryPowOfTwo();
        thingsToTestTogether = new ArrayList<>(Arrays.asList(
                makeThingsToTestTogetherUsing(null),
                makeThingsToTestTogetherUsing(ms),
                makeThingsToTestTogetherUsing(ms),
                makeThingsToTestTogetherUsing(ms2),
                makeThingsToTestTogetherUsing(ms2)
        ));
    }


    private ThingsToTestTogether makeThingsToTestTogetherUsing(MemoizationStore memoizationStore) throws NoSuchAlgorithmException {
        List<byte[]> leafValues = new ArrayList<>();
        MerkleTree merkleTree = new MerkleTree(MessageDigest.getInstance("SHA-256"), leafValues::get, leafValues::size);
        return new ThingsToTestTogether(merkleTree, leafValues, memoizationStore);
    }


    @Test
    public void expectedRootFromEmptyMerkleTree() {
        String emptyRootHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        thingsToTestTogether.forEach(sut -> assertThat(bytesToString(sut.theTree.currentRoot()), is(emptyRootHash)));
    }


    @Test
    public void expectedRootFromMerkleTreeWithLeavesForTree() {
        thingsToTestTogether.forEach(sut -> {
            MerkleTree sutMerkleTree = sut.theTree;
            List<byte[]> leafValues = sut.theLeaves;

            leafValues.add(TEST_INPUTS.get(0));
            assertThat(bytesToString(sutMerkleTree.currentRoot()), is("6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d"));

            leafValues.add(TEST_INPUTS.get(1));
            assertThat(bytesToString(sutMerkleTree.currentRoot()), is("fac54203e7cc696cf0dfcb42c92a1d9dbaf70ad9e621f4bd8d98662f00e3c125"));

            leafValues.add(TEST_INPUTS.get(2));
            assertThat(bytesToString(sutMerkleTree.currentRoot()), is("aeb6bcfe274b70a14fb067a5e5578264db0fa9b51af5e0ba159158f329e06e77"));

            leafValues.add(TEST_INPUTS.get(3));
            assertThat(bytesToString(sutMerkleTree.currentRoot()), is("d37ee418976dd95753c1c73862b9398fa2a2cf9b4ff0fdfe8b30cd95209614b7"));

            leafValues.add(TEST_INPUTS.get(4));
            assertThat(bytesToString(sutMerkleTree.currentRoot()), is("4e3bbb1f7b478dcfe71fb631631519a3bca12c9aefca1612bfce4c13a86264d4"));

            leafValues.add(TEST_INPUTS.get(5));
            assertThat(bytesToString(sutMerkleTree.currentRoot()), is("76e67dadbcdf1e10e1b74ddc608abd2f98dfb16fbce75277b5232a127f2087ef"));

            leafValues.add(TEST_INPUTS.get(6));
            assertThat(bytesToString(sutMerkleTree.currentRoot()), is("ddb89be403809e325750d3d263cd78929c2942b7942a34b77e122c9594a74c8c"));

            leafValues.add(TEST_INPUTS.get(7));
            assertThat(bytesToString(sutMerkleTree.currentRoot()), is("5dc9da79a70659a9ad559cb701ded9a2ab9d823aad2f4960cfe370eff4604328"));
        });
    }
    
    @Test
    public void expectedAuditPathForSnapshotSize() {
        thingsToTestTogether.forEach(sut -> {
            List<byte[]> leafValues = sut.theLeaves;
            MerkleTree merkleTree = sut.theTree;

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
        });
    }
//
//    @Test
//    public void expectedConsistencySetForSnapshots() {
//        for (byte[] testInput : TEST_INPUTS) {
//            leafValues.add(testInput);
//        }
//
//        List<byte[]> consistencySet1 = merkleTree.snapshotConsistency(1,1);
//        assertThat(consistencySet1.size(), is(0));
//
//        List<byte[]> consistencySet2 = merkleTree.snapshotConsistency(1,8);
//        assertThat(bytesToString(consistencySet2), is(Arrays.asList(
//                "96a296d224f285c67bee93c30f8a309157f0daa35dc5b87e410b78630a09cfc7",
//                "5f083f0a1a33ca076a95279832580db3e0ef4584bdff1f54c8a360f50de3031e",
//                "6b47aaf29ee3c2af9af889bc1fb9254dabd31177f16232dd6aab035ca39bf6e4")));
//
//        List<byte[]> consistencySet3 = merkleTree.snapshotConsistency(6,8);
//        assertThat(bytesToString(consistencySet3), is(Arrays.asList(
//                "0ebc5d3437fbe2db158b9f126a1d118e308181031d0a949f8dededebc558ef6a",
//                "ca854ea128ed050b41b35ffc1b87b8eb2bde461e9e3b5596ece6b9d5975a0ae0",
//                "d37ee418976dd95753c1c73862b9398fa2a2cf9b4ff0fdfe8b30cd95209614b7")));
//
//        List<byte[]> consistencySet4 = merkleTree.snapshotConsistency(2,5);
//        assertThat(bytesToString(consistencySet4), is(Arrays.asList(
//                "5f083f0a1a33ca076a95279832580db3e0ef4584bdff1f54c8a360f50de3031e",
//                "bc1a0643b12e4d2d7c77918f44e0f4f79a838b6cf9ec5b5c283e1f4d88599e6b")));
//    }
//
    @Test
    public void property_rootHashFromMemoizedTreeIsSameAsRootHashFromNonMemoizedTree() {
        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(1, 1000))
                .checkAssert(entryStrings -> {
                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());

                    MemoizationStore inMemoryMs = new InMemory();
                    new MerkleTree(Util.sha256Instance(), entries::get, entries::size, inMemoryMs);

                    MemoizationStore inMemoryPowOfTwoMs = new InMemoryPowOfTwo();
                    new MerkleTree(Util.sha256Instance(), entries::get, entries::size, inMemoryPowOfTwoMs);

                    MerkleTree memoizedTree = new MerkleTree(Util.sha256Instance(), entries::get, entries::size, inMemoryMs);
                    MerkleTree memoizedPowOfTwoTree = new MerkleTree(Util.sha256Instance(), entries::get, entries::size, inMemoryPowOfTwoMs);
                    MerkleTree nonMemoizedTree = new MerkleTree(Util.sha256Instance(), entries::get, entries::size);

                    assertThat(bytesToString(memoizedTree.currentRoot()), is(bytesToString(nonMemoizedTree.currentRoot())));
                    assertThat(bytesToString(memoizedPowOfTwoTree.currentRoot()), is(bytesToString(nonMemoizedTree.currentRoot())));

                });
    }

    @Test
    public void property_canConstructRootHashFromLeafAndAuditPath() throws Exception {
        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(1,1000), integers().between(0,999))
                .assuming((entries, leafIndex) -> leafIndex < entries.size())
                .checkAssert((entryStrings, leafIndex) -> {
                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());
                    MerkleTree merkleTree = makeMerkleTree(entries);
                    List<byte[]> auditPath = merkleTree.pathToRootAtSnapshot(leafIndex, entries.size());
                    assertThat(MerkleTreeVerification.isValidAuditProof(merkleTree.currentRoot(), entries.size(), leafIndex, auditPath, entries.get(leafIndex)), is(true));
                });
    }
//
//    @Test
//    public void property_canVerifyConsistencyProof() {
//        qt().forAll(lists().allListsOf(strings().numeric()).ofSizeBetween(2,1000), integers().between(1,1000), integers().between(1,1000))
//                .assuming((entries, low, high) -> low <= high && high <= entries.size())
//                .check((entryStrings, low, high) -> {
//                    List<byte[]> entries = entryStrings.stream().map(String::getBytes).collect(toList());
//                    MerkleTree merkleTree = makeMerkleTree(entries);
//                    byte[] lowRoot = makeMerkleTree(entries.subList(0, low)).currentRoot();
//                    byte[] highRoot = makeMerkleTree(entries.subList(0, high)).currentRoot();
//                    List<byte[]> consistencyProof = merkleTree.snapshotConsistency(low, high);
//
//                    return MerkleTreeVerification.isValidConsistencyProof(low, lowRoot, high, highRoot, consistencyProof);
//                }
//        );
//    }

    private MerkleTree makeMerkleTree(List<byte[]> entries) {
        return new MerkleTree(Util.sha256Instance(), entries::get, entries::size);
    }

    private List<String> bytesToString(List<byte[]> listOfByteArrays) {
        return listOfByteArrays.stream().map(this::bytesToString).collect(toList());
    }

    private String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
}

