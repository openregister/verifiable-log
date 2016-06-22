package uk.gov.verifiablelog.merkletree;

import javax.xml.bind.DatatypeConverter;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class TestUtil {
    public static  List<String> bytesToString(List<byte[]> listOfByteArrays) {
        return listOfByteArrays.stream().map(TestUtil::bytesToString).collect(toList());
    }

    public static  String bytesToString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }

    public static byte[] stringToBytes(String input) {
        return DatatypeConverter.parseHexBinary(input);
    }
}
