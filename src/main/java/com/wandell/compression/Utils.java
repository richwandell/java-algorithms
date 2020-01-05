package com.wandell.compression;

public class Utils {

    public static byte[] intToByteArray(int by) {
        return new byte[] {
                (byte) ((by & 0xFF000000) >> 24),
                (byte) ((by & 0x00FF0000) >> 16),
                (byte) ((by & 0x0000FF00) >> 8),
                (byte) ((by & 0x000000FF))
        };
    }

    public static int getNumBits(int b) {
        return (int) Math.ceil(Math.log(b+1) / Math.log(2));
    }

}
