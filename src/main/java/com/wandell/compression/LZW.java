package com.wandell.compression;


import com.github.jinahya.bit.io.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.wandell.compression.Utils.getNumBits;
import static com.wandell.compression.Utils.intToByteArray;

public class LZW {

    private static HashMap<ByteArray, Integer> lastBd;

    private static class Compressor {

        private HashMap<ByteArray, Integer> bd;
        private byte[] bData;

        private Compressor(byte[] data) {
            bd = new LinkedHashMap<>();
            for(int i = 0; i < 256; i++) {
                bd.put(new ByteArray(new byte[]{
                        0,
                        0,
                        0,
                        (byte)i
                }), bd.size());
            }
            bData = data;
        }

        private byte[] compressBytes() {
            ArrayList<Integer> totalCompressed = new ArrayList<>();
            int maxNumber = 0;
            for (int i = 0; i < bData.length; i++) {
                Byte by = bData[i];

                ByteArray ba = new ByteArray(intToByteArray(by & 0xFF));
                if (bd.containsKey(ba)) {
                    ArrayList<Integer> compressed = new ArrayList<>();
                    Integer value = (int)by & 0xFF;
                    compressed.add(value);
                    if (value > maxNumber) maxNumber = value;
                    while (true) {
                        i++;
                        if (i == bData.length) {
                            totalCompressed.addAll(compressed);
                            break;
                        }
                        by = bData[i];
                        int po = (int)(by & 0xFF);
                        if (po > maxNumber) maxNumber = po;
                        compressed.add(po);
                        byte[] byteArrayBytes = new byte[compressed.size() * 4];
                        int compressedKeyIndex = 0;
                        for (int j = 0; j < byteArrayBytes.length; j+=4) {
                            byte[] tmpByteArray = intToByteArray(compressed.get(compressedKeyIndex));
                            byteArrayBytes[j] = tmpByteArray[0];
                            byteArrayBytes[j + 1] = tmpByteArray[1];
                            byteArrayBytes[j + 2] = tmpByteArray[2];
                            byteArrayBytes[j + 3] = tmpByteArray[3];
                            compressedKeyIndex++;
                        }

                        ByteArray byteArrayKey = new ByteArray(byteArrayBytes);
                        if (bd.containsKey(byteArrayKey)) {
                            compressed = new ArrayList<>();
                            value = bd.get(byteArrayKey);
                            if (value > maxNumber) maxNumber = value;
                            compressed.add(value);
                        } else {
                            int n = bd.size();
                            bd.put(byteArrayKey, n);
                            totalCompressed.addAll(compressed);
                            break;
                        }
                    }
                } else {
                    bd.put(ba, bd.size());
                    int po = (int)(by & 0xFF);
                    if (po > maxNumber) maxNumber = po;
                    totalCompressed.add(po);
                }
            }

            ArrayByteOutput abo = new ArrayByteOutput();
            BitOutput bo = new DefaultBitOutput(abo);
            int maxBits = getNumBits(maxNumber);
            try {
                bo.writeInt(true,32, maxBits);
                bo.writeInt(true, 32, totalCompressed.size());
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
    }

    private static class Decompressor {

        private byte[] bData;

        private HashMap<ByteArray, Integer> dbAtoI;
        private HashMap<Integer, ByteArray> dbItoA;

        private Decompressor(byte[] data) {
            dbAtoI = new LinkedHashMap<>();
            dbItoA = new LinkedHashMap<>();
            for(int i = 0; i < 256; i++) {
                var ba = ByteArray.of(i);
                dbAtoI.put(ba, i);
                dbItoA.put(i, ba);
            }
            bData = data;
        }

        private byte[] decompressBytes() {
            ArrayByteInput abi = new ArrayByteInput(bData);
            BitInput bi = new DefaultBitInput(abi);

            var totalDecompressed = new byte[0];
            int totalIndex = 0;
            var sequence = new ArrayList<Byte>();

            try {
                int maxBits = bi.readInt(true, 32);
                int numNums = bi.readInt(true, 32);
                totalDecompressed = new byte[numNums];
                int currentNum = 0;
                while (true) {
                    if (currentNum == numNums) break;
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
                            sequence.addAll(ByteArray.of(val));
                        } else {
                            var tmpValue = dbItoA.get(val);
                            sequence.addAll(tmpValue);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }

                    var key = ByteArray.of(sequence);
                    if (!dbAtoI.containsKey(key)) {
                        dbAtoI.put(key, dbAtoI.size());
                        dbItoA.put(dbItoA.size(), key);
                        if (totalIndex + sequence.size() >= totalDecompressed.length) {
                            byte[] tmpByte = new byte[totalDecompressed.length + 1000];
                            System.arraycopy(totalDecompressed, 0, tmpByte, 0, totalDecompressed.length);
                            totalDecompressed = tmpByte;
                        }
                        for(int j = 3; j < sequence.size(); j+=4) {
                            totalDecompressed[totalIndex] = sequence.get(j);
                            totalIndex++;
                        }
                        sequence = new ArrayList<Byte>();
                    }
                    currentNum++;
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }

            if (sequence.size() > 0) {
                if (totalIndex + sequence.size() >= totalDecompressed.length) {
                    byte[] tmpByte = new byte[totalDecompressed.length + 500];
                    System.arraycopy(totalDecompressed, 0, tmpByte, 0, totalDecompressed.length);
                    totalDecompressed = tmpByte;
                }
                for(int j = 3; j < sequence.size(); j+=4) {
                    totalDecompressed[totalIndex] = sequence.get(j);
                    totalIndex++;
                }
            }
            byte[] returnByte = new byte[totalIndex];
            System.arraycopy(totalDecompressed, 0, returnByte, 0, totalIndex);
            return returnByte;
        }
    }

    public static byte[] decompress(byte[] data) {
        Decompressor decompressor = new Decompressor(data);
        return decompressor.decompressBytes();
    }

    public static byte[] compress(byte[] data) {
        Compressor compressor = new Compressor(data);
        return compressor.compressBytes();
    }
}
