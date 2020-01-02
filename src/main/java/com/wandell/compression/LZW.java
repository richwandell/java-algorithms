package com.wandell.compression;


import com.github.jinahya.bit.io.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class LZW {

    private HashMap<String, Integer> cd;
    private String cData;
    private String[] dd;
    private byte[] dData;

    private LZW(String data) {
        cd = new LinkedHashMap<>();

        for(int i = 0; i < 256; i++) {
            cd.put(String.valueOf((char)i), cd.size());
        }
        cData = data;
    }

    private LZW(byte[] data) {
        dd = new String[256];
        for(int i = 0; i < 256; i++) {
            dd[i] = String.valueOf((char)i);
        }
        cd = new LinkedHashMap<>();
        for(int i = 0; i < 256; i++) {
            cd.put(String.valueOf((char)i), cd.size());
        }
        dData = data;
    }

    public static String decompress(byte[] data) {
        LZW decompressor = new LZW(data);
        return decompressor.decompressBytes();
    }

    private String decompressBytes() {
        ArrayByteInput abi = new ArrayByteInput(dData);
        BitInput bi = new DefaultBitInput(abi);

        String totalDecompressed = "";
        String sequence = "";
        try {
            int maxBits = bi.readInt(true, 32);
            int ddIndex = 256;
            while (true) {
                boolean dic = true;
                boolean maxBit = bi.readBoolean();
                int val = 0;
                if (maxBit) {
                    val = bi.readInt(true, maxBits);
                } else {
                    val = bi.readInt(true, 7);
                }

                if (val < 256) {
                    dic = false;
                }

                try {
                    if (!dic) {
                        sequence += String.valueOf((char)val);
                    } else {
                        sequence += dd[val];
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

                if (!cd.containsKey(sequence)) {
//                    if (ddIndex < 4096) {
                        if (ddIndex == dd.length) {
                            String[] newArray = new String[dd.length + 1024];
                            System.arraycopy(dd, 0, newArray, 0, dd.length);
                            dd = newArray;
                        }
                        dd[ddIndex] = sequence;
                        ddIndex++;
                        cd.put(sequence, 1);
//                    }
                    totalDecompressed += sequence;
                    sequence = "";
                }
            }
        } catch (Exception ignored) { }
        totalDecompressed += sequence;
        return totalDecompressed;
    }

    private byte[] compressString() {
        ArrayList<Integer> totalCompressed = new ArrayList<>();

        int maxNumber = 0;
        for(int i = 0; i < cData.length(); i++) {
            String ch = String.valueOf(cData.charAt(i));

            if (cd.containsKey(ch)) {
                ArrayList<Integer> compressed = new ArrayList<>();
                Integer value = cd.get(ch);
                compressed.add(value);
                if (value > maxNumber) maxNumber = value;
                String compressedKey = ch;
                while(true) {
                    i++;
                    if (i == cData.length()) {
                        totalCompressed.addAll(compressed);
                        break;
                    }
                    ch = String.valueOf(cData.charAt(i));
                    compressedKey += ch;
                    int po = (int)ch.charAt(0);
                    if (po > maxNumber) maxNumber = po;
                    compressed.add(po);
                    if (cd.containsKey(compressedKey)) {
                        compressed = new ArrayList<>();
                        value = cd.get(compressedKey);
                        if (value > maxNumber) maxNumber = value;
                        compressed.add(value);
                    } else {
                        int n = cd.size();
                        cd.put(compressedKey, n);
                        totalCompressed.addAll(compressed);
                        break;
                    }
                }
            } else {
                cd.put(ch, cd.size());
                int po = (int)ch.charAt(0);
                if (po > maxNumber) maxNumber = po;
                totalCompressed.add(po);
            }
        }

        ArrayByteOutput abo = new ArrayByteOutput();
        BitOutput bo = new DefaultBitOutput(abo);

        int maxBits = getNumBits(maxNumber);

        try {
            bo.writeInt(true,32, maxBits);
            for(int i = 0; i < totalCompressed.size(); i++) {
                int num = totalCompressed.get(i);
                boolean maxBit = num > 127;
                if (maxBit) {
                    bo.writeBoolean(true);
                    bo.writeInt(true, maxBits, num);
                } else {
                    bo.writeBoolean(false);
                    bo.writeInt(true, 7, num);
                }
            }
            bo.align(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] returnBytes = abo.getTheTarget();

        return returnBytes;
    }

    public static byte[] compress(String data){
        LZW compressor = new LZW(data);
        return compressor.compressString();
    }

    public static int getNumBits(int b) {
        if (b > 2147483647) return 32;
        if (b > 1073741823) return 31;
        if (b > 536870911) return 30;
        if (b > 268435455) return 29;
        if (b > 134217727) return 28;
        if (b > 67108863) return 27;
        if (b > 33554431) return 26;
        if (b > 16777215) return 25;
        if (b > 8388607) return 24;
        if (b > 4194303) return 23;
        if (b > 2097151) return 22;
        if (b > 1048575) return 21;
        if (b > 524287) return 20;
        if (b > 262143) return 19;
        if (b > 131071) return 18;
        if (b > 65535) return 17;
        if (b > 32767) return 16;
        if (b > 16383) return 15;
        if (b > 8191) return 14;
        if (b > 4095) return 13;
        if (b > 2047) return 12;
        if (b > 1023) return 11;
        if (b > 511) return 10;
        if (b > 255) return 9;
        if (b > 127) return 8;
        if (b > 63) return 7;
        if (b > 31) return 6;
        if (b > 15) return 5;
        if (b > 7) return 4;
        if (b > 3) return 3;
        if (b > 1) return 2;
        return 1;
    }
}
