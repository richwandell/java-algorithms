package com.wandell.compression;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import static com.wandell.compression.LZWTest.*;
import static com.wandell.compression.Utils.getResourceFile;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class HuffmanTest {
    @Test
    void testIpsum() {
        try {
            var file = getResourceFile(IPSUM_RESOURCE);
            var inputBytes = Files.readAllBytes(file.toPath());
            var compressed = Huffman.compress(inputBytes);
            var decompressed = Huffman.decompress(compressed);

            assertArrayEquals(inputBytes, decompressed);

        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testPDF() {
        try {
            var file = getResourceFile(PDF_RESOURCE);
            var inputBytes = Files.readAllBytes(file.toPath());
            var compressed = Huffman.compress(inputBytes);
            var decompressed = Huffman.decompress(compressed);

            assertArrayEquals(inputBytes, decompressed);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testBible() {
        try {
            var file = getResourceFile(BIBLE_RESOURCE);
            var inputBytes = Files.readAllBytes(file.toPath());
            var compressed = Huffman.compress(inputBytes);
            var decompressed = Huffman.decompress(compressed);

            assertArrayEquals(inputBytes, decompressed);
        } catch (Exception e) {
            fail();
        }
    }

    void compress() {
        try {
            var resource = BIBLE_RESOURCE;
            var file = getResourceFile(resource);
            var inputBytes = Files.readAllBytes(file.toPath());
            var compressed = Huffman.compress(inputBytes);

            file = new File(resource + ".wch");
            Files.write(file.toPath(), compressed);

            System.out.println("Input: " + inputBytes.length + " Output: " + compressed.length);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    void decompress() {
        try {
            var resource = BIBLE_RESOURCE;
            var file = new File(resource + ".wch");
            byte[] inputBytes = Files.readAllBytes(file.toPath());
            byte[] decompressed = Huffman.decompress(inputBytes);

            file = new File(resource);
            Files.write(file.toPath(), decompressed);

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
