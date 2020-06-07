package com.geminifile.core.fileparser.netfilemanager;

import java.io.Serializable;

public class NetFileBlock implements Serializable {

    // This class handles file separated into blocks.

    private byte[] block;
    private int size;
    private int blockNum;

    public NetFileBlock(byte[] block, int size, int blockNum) {
        this.block = block;
        this.size = size;
        this.blockNum = blockNum;
    }

    public byte[] getBlock() {
        return block;
    }

    public int getSize() {
        return size;
    }

    public int getBlockNum() {
        return blockNum;
    }

}
