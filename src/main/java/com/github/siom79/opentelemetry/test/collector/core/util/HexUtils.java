package com.github.siom79.opentelemetry.test.collector.core.util;

public class HexUtils {

    private HexUtils() {}

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
