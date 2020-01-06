package com.wandell.compression;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import static com.wandell.compression.LZWTest.BIBLE_RESOURCE;
import static com.wandell.compression.LZWTest.IPSUM_RESOURCE;
import static com.wandell.compression.Utils.getResourceFile;

public class HuffmanTest {
    @Test
    void testCompress() {
        try {
            var resource = IPSUM_RESOURCE;
            var file = getResourceFile(resource);
            var inputBytes = Files.readAllBytes(file.toPath());
            var compressed = Huffman.compress(inputBytes);

            file = new File(resource + ".wc");
            Files.write(file.toPath(), compressed);

            System.out.println("Input: " + inputBytes.length + " Output: " + compressed.length);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testDecompress() {
        try {
            var resource = IPSUM_RESOURCE;
            var file = new File(resource + ".wc");
            byte[] inputBytes = Files.readAllBytes(file.toPath());
            byte[] decompressed = Huffman.decompress(inputBytes);

            file = new File(resource);
            Files.write(file.toPath(), decompressed);

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
