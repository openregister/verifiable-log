package uk.gov.verifiablelog;

import uk.gov.verifiablelog.dao.memoization.MemoizationStore;
import uk.gov.verifiablelog.dao.MerkleLeafDAO;
import uk.gov.verifiablelog.merkletree.MerkleUtil;

import javax.xml.bind.DatatypeConverter;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class TestUtil {
    public static VerifiableLog makeVerifiableLog(List<byte[]> entries) {
        return new VerifiableLog(MerkleUtil.sha256Instance(), new ListMerkleLeafDAO(entries));
    }

    public static VerifiableLog makeVerifiableLog(List<byte[]> entries, MemoizationStore memoizationStore) {
        return new VerifiableLog(MerkleUtil.sha256Instance(), new ListMerkleLeafDAO(entries), memoizationStore);
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

    private static class ListMerkleLeafDAO implements MerkleLeafDAO {
        private final List<byte[]> leafList;

        public ListMerkleLeafDAO(List<byte[]> leafList) {
            this.leafList = leafList;
        }

        @Override
        public byte[] getLeafValue(int leafIndex) {
            return leafList.get(leafIndex);
        }

        @Override
        public int totalLeaves() {
            return leafList.size();
        }
    }
}
