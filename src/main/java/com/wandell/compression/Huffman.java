package com.wandell.compression;

import com.github.jinahya.bit.io.*;

import java.io.IOException;
import java.util.*;

import static com.wandell.compression.Utils.getNumBits;

public class Huffman {

    private static class ByteNode implements Comparable {

        private static HashMap<ByteNode, ByteNode> byteNodeMap = new HashMap<>();
        private ByteNode parent;
        private boolean isLeaf;
        private boolean isRoot;
        private int frequency;
        private byte value;
        private ByteNode left;
        private ByteNode right;
        private int index = 0;

        private ByteNode(Byte value) {
            this.value = value;
        }

        private ByteNode(Byte value, int frequency) {
            isLeaf = true;
            this.value = value;
            this.frequency = frequency;
            byteNodeMap.put(this, this);
        }

        private ByteNode(ByteNode left, ByteNode right) {
            isLeaf = false;
            this.left = left;
            this.right = right;
            frequency = left.getFrequency() + right.getFrequency();
            left.parent = this;
            right.parent = this;
            left.index = 0;
            right.index = 1;
        }

        private String getStringIndex() {
            if (parent != null) {
                return parent.getStringIndex() + String.valueOf(index);
            }
            if (isRoot) {
                return "";
            }
            return String.valueOf(index);
        }

        private int getIndex() {
            if (parent != null) {
                return parent.getIndex() << 1 | index;
            }
            return index;
        }

        private byte getValue() {
            return value;
        }

        private int getFrequency() {
            return frequency;
        }

        @Override
        public int compareTo(Object o) {
            int a = frequency;
            int b = ((ByteNode)o).getFrequency();
            if (a > b) return 1;
            if (a < b) return -1;
            return 0;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ByteNode)) {
                return false;
            }
            return (int)value == (int)((ByteNode)other).getValue();
        }

        @Override
        public int hashCode() {
            return value;
        }

        private boolean isLeaf() {
            return isLeaf;
        }
    }

    private static class Compressor {
        private byte[] bData;
        private String sData;
        private PriorityQueue<ByteNode> byteQueue;
        private LinkedHashMap<Byte, Integer> frequency;

        private Compressor(String data) {
            sData = data;
            byteQueue = new PriorityQueue<>();
            frequency = new LinkedHashMap<>();
        }

        private Compressor(byte[] data) {
            bData = data;
            byteQueue = new PriorityQueue<>();
            frequency = new LinkedHashMap<>();
        }

        private byte[] compressString() {
            for (int i = 0; i < sData.length(); i++) {
                Byte b = (byte) sData.charAt(i);
                if (frequency.containsKey(b)) {
                    int currentVal = frequency.get(b);
                    frequency.put(b, currentVal + 1);
                } else {
                    frequency.put(b, 1);
                }
            }
            return compress(0);
        }

        private byte[] compressBytes() {
            for (int i = 0; i < bData.length; i++) {
                Byte b = bData[i];
                if (frequency.containsKey(b)) {
                    int currentVal = frequency.get(b);
                    frequency.put(b, currentVal + 1);
                } else {
                    frequency.put(b, 1);
                }
            }
            return compress(1);
        }

        private byte[] compress(int type) {
            for(Map.Entry<Byte, Integer> byteNode : frequency.entrySet()) {
                byteQueue.add(new ByteNode(byteNode.getKey(), byteNode.getValue()));
            }
            ByteNode root = new ByteNode((byte)0);
            while(!byteQueue.isEmpty()) {
                ByteNode byteNode1 = byteQueue.poll();
                ByteNode byteNode2 = byteQueue.poll();
                if (byteNode2 == null) {
                    root = byteNode1;
                    root.isRoot = true;
                    break;
                }
                ByteNode newNode = new ByteNode(byteNode1, byteNode2);
                byteQueue.add(newNode);
            }

            ArrayByteOutput abo = new ArrayByteOutput();
            BitOutput bo = new DefaultBitOutput(abo);
            Collection<ByteNode> values = ByteNode.byteNodeMap.values();
            int keyLength = 0;
            int valLength = 0;
            for(ByteNode entry : values) {
                int index = entry.getIndex();
                int numIndexBits = getNumBits(index);

                int value = entry.getValue() & 0xFF;
                int numValueBits = getNumBits(value);

                if (numIndexBits > keyLength) {
                    keyLength = numIndexBits;
                }
                if (numValueBits > valLength){
                    valLength = numValueBits;
                }
            }

            try {
                bo.writeInt(true, 32, keyLength);
                bo.writeInt(true, 32, valLength);
                bo.writeInt(true, 32, values.size());
                for(ByteNode entry : values) {
                    bo.writeInt(true, keyLength, entry.getIndex());
                    bo.writeInt(true, valLength, entry.getValue() & 0xFF);
                }

                if (type == 0) {
                    for (int i = 0; i < sData.length(); i++) {
                        ByteNode byteNodeKey = new ByteNode((byte) sData.charAt(i));
                        ByteNode byteNode = ByteNode.byteNodeMap.get(byteNodeKey);
                        bo.writeInt(true, getNumBits(byteNode.getValue() & 0xFF), byteNode.getValue() & 0xFF);
                    }
                } else {
                    for (int i = 0; i < bData.length; i++) {
                        ByteNode byteNodeKey = new ByteNode(bData[i]);
                        ByteNode byteNode = ByteNode.byteNodeMap.get(byteNodeKey);
                        String stringIndex = byteNode.getStringIndex();
                        int intIndex = byteNode.getIndex();
                        int length = stringIndex.length();
                        try {
                            bo.writeInt(true, length, intIndex);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                bo.align(1);
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
            }

            return abo.getTheTarget();
        }
    }

    private static class Decompressor {
        private final byte[] bData;

        private Decompressor(byte[] data) {
            bData = data;
        }

        private byte[] decompressBytes() {
            ArrayByteInput abi = new ArrayByteInput(bData);
            BitInput bi = new DefaultBitInput(abi);

            try {
                int keyLength = bi.readInt(true, 32);
                int valLength = bi.readInt(true, 32);
                int numValues = bi.readInt(true, 32);
                HashMap<Integer, Integer> byteMap = new HashMap<>();
                for(int i = 0; i < numValues; i++) {
                    int key = bi.readInt(true, keyLength);
                    int val = bi.readInt(true, valLength);
                    byteMap.put(key, val);
                }

                int currentValue = 0;
                int index = 0;
                while (true) {

                    boolean currentBit = bi.readBoolean();

                }

//                System.out.println(byteMap.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }

            return bData;
        }
    }

    public static byte[] decompress(byte[] data) {
        Decompressor d = new Decompressor(data);
        return d.decompressBytes();
    }

    public static byte[] compress(byte[] data) {
        Compressor c = new Compressor(data);
        return c.compressBytes();
    }

    public static byte[] compress(String data) {
        Compressor c = new Compressor(data);
        return c.compressString();
    }
}
