package com.wandell.compression;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class LZW {

    private String data;

    private LinkedHashMap<String, Integer> dictionary;


    private LZW(String data) {
        this.data = data;
        dictionary = new LinkedHashMap<>();
//        for(int i = 0; i < 255; i++) {
//            dictionary.put(String.valueOf((char)i), dictionary.size());
//        }
    }

    public byte[] compressString(String data) {
        ArrayList<Integer> totalCompressed = new ArrayList<>();


        for(int i = 0; i < data.length(); i++) {
            String ch = String.valueOf(data.charAt(i));

            if (dictionary.containsKey(ch)) {
                ArrayList<Integer> compressed = new ArrayList<>();
                Integer value = dictionary.get(ch);
                compressed.add(value);
                String compressedKey = ch;
                while(true) {
                    i++;
                    if (i == data.length()) {
                        totalCompressed.addAll(compressed);
                        break;
                    }
                    ch = String.valueOf(data.charAt(i));
                    compressedKey += ch;
                    compressed.add((int)ch.charAt(0));
                    if (dictionary.containsKey(compressedKey)) {
                        compressed = new ArrayList<>();
                        value = dictionary.get(compressedKey);
                        compressed.add(value);
                    } else {
                        dictionary.put(compressedKey, dictionary.size() + 255);
                        totalCompressed.addAll(compressed);
                        break;
                    }
                }
            } else {
                dictionary.put(ch, dictionary.size() + 255);
                totalCompressed.add((int)ch.charAt(0));
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
        return compressor.compressString(data);
    }
}
