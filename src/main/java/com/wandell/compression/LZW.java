package com.wandell.compression;


import com.github.jinahya.bit.io.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.wandell.compression.Utils.getNumBits;
import static com.wandell.compression.Utils.intToByteArray;

public class LZW {

    private static class Compressor {

        private HashMap<String, Integer> sd;
        private String sData;
        private HashMap<ByteArray, Integer> bd;
        private byte[] bData;

        private Compressor(String data) {
            sd = new LinkedHashMap<>();
            for(int i = 0; i < 256; i++) {
                sd.put(String.valueOf((char)i), sd.size());
            }
            sData = data;
        }

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

                ByteArray ba = new ByteArray(intToByteArray(by));
                if (bd.containsKey(ba)) {
                    ArrayList<Integer> compressed = new ArrayList<>();
                    Integer value = (int)by;
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

        private byte[] compressString() {
            ArrayList<Integer> totalCompressed = new ArrayList<>();

            int maxNumber = 0;
            for(int i = 0; i < sData.length(); i++) {
                String ch = String.valueOf(sData.charAt(i));

                if (sd.containsKey(ch)) {
                    ArrayList<Integer> compressed = new ArrayList<>();
                    Integer value = sd.get(ch);
                    compressed.add(value);
                    if (value > maxNumber) maxNumber = value;
                    String compressedKey = ch;
                    while(true) {
                        i++;
                        if (i == sData.length()) {
                            totalCompressed.addAll(compressed);
                            break;
                        }
                        ch = String.valueOf(sData.charAt(i));
                        compressedKey += ch;
                        int po = (int)ch.charAt(0);
                        if (po > maxNumber) maxNumber = po;
                        compressed.add(po);
                        if (sd.containsKey(compressedKey)) {
                            compressed = new ArrayList<>();
                            value = sd.get(compressedKey);
                            if (value > maxNumber) maxNumber = value;
                            compressed.add(value);
                        } else {
                            int n = sd.size();
                            sd.put(compressedKey, n);
                            totalCompressed.addAll(compressed);
                            break;
                        }
                    }
                } else {
                    sd.put(ch, sd.size());
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

        private String[] dd;
        private HashMap<String, Integer> cd;
        private byte[] dData;

        private Decompressor(byte[] data) {
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

        private String decompressBytes() {
            ArrayByteInput abi = new ArrayByteInput(dData);
            BitInput bi = new DefaultBitInput(abi);

            String totalDecompressed = "";
            String sequence = "";
            try {
                int maxBits = bi.readInt(true, 32);
                int numNums = bi.readInt(true, 32);
                int ddIndex = 256;
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
                            if (val == 0) {
                                sequence += "\0";
                            } else {
                                sequence += String.valueOf((char) val);
                            }
                        } else {
                            sequence += dd[val];
                        }
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }

                    if (!cd.containsKey(sequence)) {
                        if (ddIndex == dd.length) {
                            String[] newArray = new String[dd.length + 1024];
                            System.arraycopy(dd, 0, newArray, 0, dd.length);
                            dd = newArray;
                        }
                        dd[ddIndex] = sequence;
                        ddIndex++;
                        cd.put(sequence, 1);

                        totalDecompressed += sequence;
                        sequence = "";
                    }
                    currentNum++;
                }
            } catch (Exception ignored) { }
            if (sequence.length() > 0) {
                totalDecompressed += sequence;
            }
            return totalDecompressed;
        }
    }

    public static String decompress(byte[] data) {
        Decompressor decompressor = new Decompressor(data);
        return decompressor.decompressBytes();
    }

    public static byte[] compress(String data){
        Compressor compressor = new Compressor(data);
        return compressor.compressString();
    }

    public static byte[] compress(byte[] data) {
        Compressor compressor = new Compressor(data);
        return compressor.compressBytes();
    }
}
