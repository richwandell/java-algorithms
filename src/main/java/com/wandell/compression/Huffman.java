package com.wandell.compression;

import com.github.jinahya.bit.io.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.*;

import static com.wandell.compression.Utils.getNumBits;

public class Huffman {

    private static class ByteNode implements Comparable {

        private static int depth = 0;
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
            updateDepth();
        }

        private void updateDepth() {
            if (left != null) {
                left.updateDepth();
            }
            if (right != null) {
                right.updateDepth();
            }
            String stringIndex = getStringIndex();
            if (stringIndex.length() > ByteNode.depth) {
                ByteNode.depth = stringIndex.length();
            }
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
        private PriorityQueue<ByteNode> byteQueue;
        private LinkedHashMap<Byte, Integer> frequency;

        private Compressor(byte[] data) {
            bData = data;
            byteQueue = new PriorityQueue<>();
            frequency = new LinkedHashMap<>();
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

                int value = entry.getValue();
                int numValueBits = getNumBits(value);

                if (numIndexBits > keyLength) {
                    keyLength = numIndexBits;
                }

                if (numValueBits > valLength){
                    valLength = numValueBits;
                }
            }

            try {
                bo.writeInt(true, 32, valLength);
                bo.writeInt(true, 32, values.size());
                bo.writeInt(true, 32, bData.length);

                for(ByteNode entry : values) {
                    bo.writeInt(true, valLength, entry.getValue());
                    bo.writeInt(true, 32, entry.getFrequency());
                }

                for (int i = 0; i < bData.length; i++) {
                    ByteNode byteNodeKey = new ByteNode(bData[i]);
                    ByteNode byteNode = ByteNode.byteNodeMap.get(byteNodeKey);
                    int intIndex = byteNode.getIndex();
                    String stringIndex = byteNode.getStringIndex();
                    StringBuilder input1 = new StringBuilder();
                    input1.append(stringIndex);
                    stringIndex = input1.reverse().toString();
                    try {
                        for(int j = 0; j < stringIndex.length(); j++) {
                            char ch = stringIndex.charAt(j);
                            if (ch == '0') {
                                bo.writeBoolean(false);
                            } else {
                                bo.writeBoolean(true);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
        private PriorityQueue<ByteNode> byteQueue;
        private LinkedHashMap<Byte, Integer> frequency;

        private Decompressor(byte[] data) {
            bData = data;
            byteQueue = new PriorityQueue<>();
            frequency = new LinkedHashMap<>();
        }

        private ByteNode createByteNodeTree(HashMap<Integer, Integer> byteMap, int keyLength) {
            ByteNode root = new ByteNode((byte)(int)byteMap.get(0));

            Integer[] keys = byteMap.keySet().toArray(new Integer[0]);
            Arrays.sort(keys);

            ByteNode current = root;

            for(Integer i : keys) {
                var tmp = byteMap.get(i);

            }

//            ByteNode current = root;
//            for(var entry : byteMap.entrySet()) {
//                var key = entry.getKey();
//                var val = entry.getValue();
//
//                int loopNum = 0;
//                while (true) {
//                    int nextBit = key >> loopNum & 0xFFFFFF;
//                    if (nextBit == 0) {
//                        current = current.getLeft();
//                    } else {
//                        current = current.getRight();
//                    }
//                    loopNum++;
//                }
//            }
            return root;
        }

        private byte[] decompressBytes() {
            ArrayByteInput abi = new ArrayByteInput(bData);
            BitInput bi = new DefaultBitInput(abi);
            ArrayList<Byte> decompressed = new ArrayList<>();

            try {
                int valLength = bi.readInt(true, 32);
                int numValues = bi.readInt(true, 32);
                int numNums = bi.readInt(true, 32);
                for(int i = 0; i < numValues; i++) {
                    int val = bi.readInt(true, valLength);
                    int f = bi.readInt(true, 32);

                    frequency.put((byte)val, f);
                }

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


                int loopNum = 0;

                while (true) {
                    if (loopNum == numNums) break;

                    int currentValue = 0;
                    int mappedByte = 0;
                    ByteNode current = root;
                    while(true) {
                        if (current.isLeaf()) {
                            int value = current.getValue();
                            System.out.println(value);
                            mappedByte = value;
                            break;
                        }
                        boolean currentByte = bi.readBoolean();
                        if (!currentByte) {
                            current = current.left;
                        } else {
                            current = current.right;
                        }
                    }


                    decompressed.add((byte)mappedByte);
                    loopNum++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return ByteArray.of(decompressed).getData();
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
}
