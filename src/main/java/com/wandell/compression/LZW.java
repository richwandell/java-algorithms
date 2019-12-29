package com.wandell.compression;


import java.util.ArrayList;
import java.util.LinkedHashMap;

public class LZW {

    private LinkedHashMap<String, Integer> cd;
    private String cData;
    private ArrayList<String> dd;
    private byte[] dData;

    private LZW(String data) {
        cd = new LinkedHashMap<>();
        cData = data;
    }

    private LZW(byte[] data) {
        dd = new ArrayList<>();
        dData = data;
    }

    public static String decompress(byte[] data) {
        LZW decompressor = new LZW(data);
        return decompressor.decompressBytes();
    }

    private String decompressBytes() {
        String totalDecompressed = "";
        int number = 0;
        String sequence = "";
        String rich;
        for(int i = 0; i < dData.length; i++) {
            if ((int) dData[i] == 0) {
                number = ((int) dData[i+1] & 0xFF) + ((int) dData[i+2] & 0xFF);
                i += 2;
            } else {
                number = (int) dData[i];
            }

            if (i == 80) {
                rich = "hi";
            }

            if (number < 0) {
                sequence += String.valueOf((char)-number);
                dd.add(sequence);
                totalDecompressed += sequence;
                sequence = "";
                continue;
            }

            try {
                sequence += dd.get(number);
                continue;
            } catch (IndexOutOfBoundsException e) {
                sequence += String.valueOf((char)number);
            }
            dd.add(sequence);
            totalDecompressed += sequence;
            sequence = "";
        }
        return totalDecompressed;
    }

    private byte[] compressString() {
        ArrayList<Integer> totalCompressed = new ArrayList<>();

        for(int i = 0; i < cData.length(); i++) {
            String ch = String.valueOf(cData.charAt(i));

            if (cd.containsKey(ch)) {
                ArrayList<Integer> compressed = new ArrayList<>();
                Integer value = cd.get(ch);
                compressed.add(value);
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
                    if (cd.size() > po) {
                        po = -po;
                    }
                    compressed.add(po);
                    if (cd.containsKey(compressedKey)) {
                        compressed = new ArrayList<>();
                        value = cd.get(compressedKey);
                        compressed.add(value);
                    } else {
                        cd.put(compressedKey, cd.size());
                        totalCompressed.addAll(compressed);
                        break;
                    }
                }
            } else {
                cd.put(ch, cd.size());
                int po = (int)ch.charAt(0);
                if (cd.size() > po) {
                    po = -po;
                }
                totalCompressed.add(po);
            }
        }

        ArrayList<Byte> bytes = new ArrayList<>();

        for(int i = 0; i < totalCompressed.size(); ++i) {
            int number = totalCompressed.get(i);

            int firstByte = 0;
            int secondByte = number;
            if (number > 255) {
                secondByte = number - 255;
                firstByte = number - secondByte;
                bytes.add((byte)"\0".charAt(0));
                bytes.add((byte)firstByte);
                bytes.add((byte)secondByte);
            } else {
                bytes.add((byte)secondByte);
            }
        }

        byte[] returnBytes = new byte[bytes.size()];
        int i = 0;
        for(Byte b : bytes) {
            returnBytes[i] = b;
            i++;
        }
        return returnBytes;
    }

    public static byte[] compress(String data){
        LZW compressor = new LZW(data);
        return compressor.compressString();
    }
}
