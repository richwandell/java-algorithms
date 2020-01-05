package com.wandell.compression;

import org.junit.jupiter.api.Test;

import static com.wandell.compression.Utils.getNumBits;
import static com.wandell.compression.Utils.intToByteArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {
    @Test
    void testIntToByteArray() {
        for(int i = 0; i < 32; i++) {
            double number = Math.pow(2, i);
            int theInt = (int)number;
            byte theByte = 1;
            byte[] theByteArray = new byte[0];
            if (i < 8) {
                theByte = (byte)number;
                theByteArray = new byte[]{0, 0, 0, theByte};
            } else if (i < 16) {
                theByte = (byte) (theInt >> 8);
                theByteArray = new byte[]{0, 0, theByte, 0};
            } else if (i < 24) {
                theByte = (byte) (theInt >> 16);
                theByteArray = new byte[]{0, theByte, 0, 0};
            } else {
                theByte = (byte) (theInt >> 24);
                theByteArray = new byte[]{theByte, 0, 0, 0};
            }
            assertArrayEquals(theByteArray, intToByteArray(theInt));
        }
    }

    @Test
    void testNumBits() {
        assertEquals(31, getNumBits(1073741824));
        assertEquals(30, getNumBits(536870911));
        assertEquals(29, getNumBits(268435456));
        assertEquals(28, getNumBits(134217728));
        assertEquals(27, getNumBits(67108864));
        assertEquals(26, getNumBits(33554432));
        assertEquals(25, getNumBits(16777216));
        assertEquals(24, getNumBits(8388608));
        assertEquals(23, getNumBits(4194304));
        assertEquals(22, getNumBits(2097152));
        assertEquals(21, getNumBits(1048576));
        assertEquals(20, getNumBits(524288));
        assertEquals(19, getNumBits(262144));
        assertEquals(18, getNumBits(131072));
        assertEquals(17, getNumBits(65536));
        assertEquals(16, getNumBits(32768));
        assertEquals(15, getNumBits(16384));
        assertEquals(14, getNumBits(8192));
        assertEquals(13, getNumBits(4096));
        assertEquals(12, getNumBits(2048));
        assertEquals(11, getNumBits(1024));
        assertEquals(10, getNumBits(512));
        assertEquals(9, getNumBits(256));
        assertEquals(8, getNumBits(128));
        assertEquals(7, getNumBits(64));
        assertEquals(6, getNumBits(32));
        assertEquals(5, getNumBits(16));
        assertEquals(4, getNumBits(8));
        assertEquals(3, getNumBits(7));
        assertEquals(3, getNumBits(6));
        assertEquals(3, getNumBits(5));
        assertEquals(3, getNumBits(4));
        assertEquals(2, getNumBits(3));
        assertEquals(2, getNumBits(2));
        assertEquals(1, getNumBits(1));
    }
}
