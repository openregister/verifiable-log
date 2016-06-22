package uk.gov.verifiablelog.merkletree;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

class MerkleTreeTestUnit {
    public MerkleTree merkleTree;
    public List<byte[]> leaves;

    public MerkleTreeTestUnit(MerkleTree merkleTree, List<byte[]> leaves) {
        this.merkleTree = merkleTree;
        this.leaves = leaves;
    }
}

public class TestUtil {
    public static MerkleTree makeMerkleTree(List<byte[]> entries) {
        return new MerkleTree(Util.sha256Instance(), entries::get, entries::size);
    }

    public static MerkleTree makeMerkleTree(List<byte[]> entries, MemoizationStore memoizationStore) {
        return new MerkleTree(Util.sha256Instance(), entries::get, entries::size, memoizationStore);
    }

    public static MerkleTreeTestUnit makeMerkleTreeTestUnit(MemoizationStore memoizationStore) {
        List<byte[]> leafValues = new ArrayList<>();
        return new MerkleTreeTestUnit(makeMerkleTree(leafValues, memoizationStore), leafValues);
    }

    public static List<String> bytesToString(List<byte[]> listOfByteArrays) {
        return listOfByteArrays.stream().map(TestUtil::bytesToString).collect(toList());
    }

    public static String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }

    public static byte[] stringToBytes(String input) {
        return DatatypeConverter.parseHexBinary(input);
    }
}
