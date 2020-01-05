package com.wandell.compression;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class HuffmanTest {
    @Test
    void testCompress() {
        try {
            File fileIn = new File("bible.txt");
            byte[] inputBytes = Files.readAllBytes(fileIn.toPath());
            byte[] compressed = Huffman.compress(inputBytes);

            File file = new File("huffman_out.txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(compressed);
            fos.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testDecompress() {
        try {
            File fileIn = new File("huffman_out.txt");
            byte[] inputBytes = Files.readAllBytes(fileIn.toPath());
            byte[] compressed = Huffman.decompress(inputBytes);

            File file = new File("huffman_out.txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(compressed);
            fos.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
