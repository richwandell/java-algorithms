package com.wandell.compression;

import org.junit.jupiter.api.Test;

import static com.wandell.compression.Utils.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {
    @Test
    void testByteArrayToInt() {
        for(int i = 0; i < 31; i++) {
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
            assertEquals(theInt, byteArrayToInt(theByteArray));
        }
    }

    @Test
    void testIntToByteArray() {
        for(int i = 0; i < 31; i++) {
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
        for(int i = 1; i < 31; i++) {
            double number = Math.pow(2, i);
            int theInt = (int)number;
            int bits = getNumBits(theInt);
            assertEquals(i+1, bits);
        }
    }
}
