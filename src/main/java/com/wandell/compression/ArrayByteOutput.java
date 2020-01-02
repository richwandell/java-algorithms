package com.wandell.compression;

import java.io.IOException;

public class ArrayByteOutput extends com.github.jinahya.bit.io.ArrayByteOutput{

    public ArrayByteOutput(byte[] target) {
        super(target);
    }

    public ArrayByteOutput() {
        super(null);
        byte[] target = new byte[1];
        setTarget(target);
        setIndex(0);
    }

    public byte[] getTheTarget() {
        return super.getTarget();
    }

    @Override
    public void write(final int value) throws IOException {
        int index = this.getIndex();
        byte[] target = getTarget();
        if (target == null) {
            target = new byte[1];
            this.setTarget(target);
            this.setIndex(0);
        } else if (target.length < index + 1) {
            byte[] newTarget = new byte[target.length + 1];
            System.arraycopy(target, 0, newTarget, 0, target.length);
            this.setTarget(newTarget);
        }

        super.write(value);
    }
}
