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
        private LinkedHashMap<Integer, Integer> frequency;

        private Compressor(byte[] data) {
            bData = data;
            byteQueue = new PriorityQueue<>();
            frequency = new LinkedHashMap<>();
        }

        private byte[] compressBytes() {
            for (int i = 0; i < bData.length; i++) {
                Byte b = bData[i];
                if (frequency.containsKey(b & 0xFF)) {
                    int currentVal = frequency.get(b & 0xFF);
                    frequency.put(b & 0xFF, currentVal + 1);
                } else {
                    frequency.put(b & 0xFF, 1);
                }
            }

            ArrayList<ByteNode> byteQueue = new ArrayList<>();

            for(Map.Entry<Integer, Integer> byteNode : frequency.entrySet()) {
                byteQueue.add(new ByteNode((byte) ((int)byteNode.getKey()), byteNode.getValue()));
            }

            Comparator<ByteNode> comparator = Comparator.comparing(node -> node.frequency);
            comparator = comparator.thenComparing(node -> node.value, Comparator.reverseOrder());
            byteQueue.sort(comparator);

            ByteNode root = new ByteNode((byte)0);
            while(!byteQueue.isEmpty()) {
                ByteNode byteNode1 = byteQueue.remove(0);
                if (byteQueue.size() == 0) {
                    root = byteNode1;
                    root.isRoot = true;
                    break;
                }
                ByteNode byteNode2 = byteQueue.remove(0);
                ByteNode newNode = new ByteNode(byteNode1, byteNode2);
                byteQueue.add(newNode);
                byteQueue.sort(comparator);
            }

            ArrayByteOutput abo = new ArrayByteOutput();
            BitOutput bo = new DefaultBitOutput(abo);
            Collection<ByteNode> values = ByteNode.byteNodeMap.values();
            int valLength = 0;
            int frequencyBits = 0;
            for(ByteNode entry : values) {
                int value = entry.getValue();
                int numValueBits = getNumBits(value);

                if (numValueBits > valLength){
                    valLength = numValueBits;
                }

                int frequency = entry.getFrequency();
                int currentFrequencyBits = getNumBits(frequency);
                if (currentFrequencyBits > frequencyBits) {
                    frequencyBits = currentFrequencyBits;
                }
            }

            try {
                bo.writeInt(true, 32, valLength);
                bo.writeInt(true, 32, values.size());
                bo.writeInt(true, 32, bData.length);
                bo.writeInt(true, 32, frequencyBits);

                for(ByteNode entry : values) {
                    bo.writeInt(true, valLength, entry.getValue());
                    bo.writeInt(true, frequencyBits, entry.getFrequency());
                }

                for (int i = 0; i < bData.length; i++) {
                    ByteNode byteNodeKey = new ByteNode(bData[i]);
                    ByteNode byteNode = ByteNode.byteNodeMap.get(byteNodeKey);
                    String stringIndex = byteNode.getStringIndex();
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
        private ArrayList<ByteNode> byteQueue;
        private LinkedHashMap<Integer, Integer> frequency;

        private Decompressor(byte[] data) {
            bData = data;
            byteQueue = new ArrayList<>();
            frequency = new LinkedHashMap<>();
        }

        private byte[] decompressBytes() {
            ArrayByteInput abi = new ArrayByteInput(bData);
            BitInput bi = new DefaultBitInput(abi);
            ArrayList<Byte> decompressed = new ArrayList<>();

            try {
                int valLength = bi.readInt(true, 32);
                int numValues = bi.readInt(true, 32);
                int numNums = bi.readInt(true, 32);
                int frequencyBits = bi.readInt(true, 32);
                for(int i = 0; i < numValues; i++) {
                    int val = bi.readInt(true, valLength);
                    int f = bi.readInt(true, frequencyBits);
                    frequency.put(val, f);
                }

                for(Map.Entry<Integer, Integer> byteNode : frequency.entrySet()) {
                    byteQueue.add(new ByteNode((byte) (int)byteNode.getKey(), byteNode.getValue()));
                }

                Comparator<ByteNode> comparator = Comparator.comparing(node -> node.frequency);
                comparator = comparator.thenComparing(node -> node.value, Comparator.reverseOrder());
                byteQueue.sort(comparator);

                ByteNode root = new ByteNode((byte)0);
                while(!byteQueue.isEmpty()) {
                    ByteNode byteNode1 = byteQueue.remove(0);
                    if (byteQueue.size() == 0) {
                        root = byteNode1;
                        root.isRoot = true;
                        break;
                    }
                    ByteNode byteNode2 = byteQueue.remove(0);
                    ByteNode newNode = new ByteNode(byteNode1, byteNode2);
                    byteQueue.add(newNode);
                    byteQueue.sort(comparator);
                }

                int loopNum = 0;
                while (true) {
                    if (loopNum == numNums) break;
                    int mappedByte = 0;
                    ByteNode current = root;
                    while(true) {
                        if (current.isLeaf()) {
                            mappedByte = current.getValue();
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
