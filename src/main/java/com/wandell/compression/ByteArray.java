package com.wandell.compression;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static com.wandell.compression.Utils.intToByteArray;

public final class ByteArray extends AbstractCollection<Byte> {
    private final byte[] data;

    public byte[] getData() {
        return data;
    }

    public static ByteArray of(int data) {
        return new ByteArray(intToByteArray(data));
    }

    public static ByteArray of(ArrayList<Byte> data) {
        byte[] byteArray = new byte[data.size()];
        int i = 0;
        for(Byte b : data) {
            byteArray[i] = (byte)b;
            i++;
        }
        return new ByteArray(byteArray);
    }

    public ByteArray(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }
        this.data = data;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ByteArray)) {
            return false;
        }
        return Arrays.equals(data, ((ByteArray)other).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return data.length > index;
            }

            @Override
            public Byte next() {
                var value = data[index];
                index++;
                return value;
            }
        };
    }

    @Override
    public int size() {
        return data.length;
    }
}