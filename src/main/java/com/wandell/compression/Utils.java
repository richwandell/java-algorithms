package com.wandell.compression;

import java.io.File;
import java.util.Objects;

public class Utils {

    public static File getResourceFile(String fileName) throws NullPointerException {
            return new File(Objects.requireNonNull(Utils.class
                    .getClassLoader()
                    .getResource(fileName))
                    .getFile());
    }

    public static byte[] intToByteArray(int by) {
        return new byte[] {
                (byte) ((by & 0xFF000000) >> 24),
                (byte) ((by & 0x00FF0000) >> 16),
                (byte) ((by & 0x0000FF00) >> 8),
                (byte) ((by & 0x000000FF))
        };
    }

    public static int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF));
    }

    public static int getNumBits(int b) {
        return (int) Math.ceil(Math.log(b+1) / Math.log(2));
    }

}
