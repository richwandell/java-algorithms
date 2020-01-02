package com.wandell.compression;


import com.github.jinahya.bit.io.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class LZW {

    private LinkedHashMap<String, Integer> cd;
    private String cData;
    private ArrayList<String> dd;
    private byte[] dData;

    private LZW(String data) {
        cd = new LinkedHashMap<>();

        for(int i = 0; i < 256; i++) {
            cd.put(String.valueOf((char)i), cd.size());
        }
        cData = data;
    }

    private LZW(byte[] data) {
        dd = new ArrayList<>();
        for(int i = 0; i < 256; i++) {
            dd.add(String.valueOf((char)i));
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
        ArrayList<Integer> data = new ArrayList<>();
        try {
            int maxBits = bi.readInt(true, 4);
            while (true) {
                boolean dic = bi.readBoolean();
                boolean maxBit = bi.readBoolean();
                int val = 0;
                if (maxBit) {
                    val = bi.readInt(true, maxBits);
                } else {
                    val = bi.readInt(true, 7);
                }
                if (dic) {
                    data.add(1);
                } else {
                    data.add(0);
                }
                data.add(val);
            }
        } catch (Exception ignored) { }

        String totalDecompressed = "";
        String sequence = "";
        for(int i = 0; i < data.size(); i+=2) {
            int a = data.get(i);
            int b = data.get(i+1);
            try {
                if (a == 0) {
                    sequence += String.valueOf((char)b);
                } else {
                    sequence += dd.get(b + 256);
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            if (!dd.contains(sequence)) {
                dd.add(sequence);
                totalDecompressed += sequence;
                sequence = "";
            }
        }
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
                compressed.add(0);
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
                    compressed.add(0);
                    compressed.add(po);
                    if (cd.containsKey(compressedKey)) {
                        compressed = new ArrayList<>();
                        value = cd.get(compressedKey);
                        if (value > maxNumber) maxNumber = value;
                        compressed.add(1);
                        compressed.add(value - 256);
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
                totalCompressed.add(0);
                totalCompressed.add(po);
            }
        }

        ArrayByteOutput abo = new ArrayByteOutput();
        BitOutput bo = new DefaultBitOutput(abo);

        int maxBits = getNumBits(maxNumber);

        try {
            bo.writeInt(true,4, maxBits);
            for(int i = 0; i < totalCompressed.size(); i++) {
                int dic = totalCompressed.get(i);
                int num = totalCompressed.get(i + 1);
                boolean maxBit = num > 127;
                if (dic == 0) {
                    bo.writeBoolean(false);
                } else {
                    bo.writeBoolean(true);
                }
                if (maxBit) {
                    bo.writeBoolean(true);
                    bo.writeInt(true, maxBits, num);
                } else {
                    bo.writeBoolean(false);
                    bo.writeInt(true, 7, num);
                }

                i++;
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
