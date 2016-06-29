package uk.gov.verifiablelog;

import uk.gov.verifiablelog.store.memoization.MemoizationStore;
import uk.gov.verifiablelog.store.MerkleLeafStore;

import javax.xml.bind.DatatypeConverter;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class TestUtil {
    public static VerifiableLog makeVerifiableLog(List<byte[]> entries) {
        return new VerifiableLog(Util.sha256Instance(), new ListMerkleLeafStore(entries));
    }

    public static VerifiableLog makeVerifiableLog(List<byte[]> entries, MemoizationStore memoizationStore) {
        return new VerifiableLog(Util.sha256Instance(), new ListMerkleLeafStore(entries), memoizationStore);
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

    private static class ListMerkleLeafStore implements MerkleLeafStore {
        private final List<byte[]> leafList;

        public ListMerkleLeafStore(List<byte[]> leafList) {
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
