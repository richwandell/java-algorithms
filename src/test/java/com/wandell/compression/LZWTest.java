package com.wandell.compression;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.wandell.compression.Utils.getNumBits;
import static com.wandell.compression.Utils.getResourceFile;
import static org.junit.jupiter.api.Assertions.*;

public class LZWTest {

    static final String IPSUM_RESOURCE = "pirateipsum.txt";
    static final String BIBLE_RESOURCE = "bible.txt";
    static final String PDF_RESOURCE = "cambridge_math_reading_list.pdf";

    @Test
    void testIpsum() {
        try {
            var file = getResourceFile(IPSUM_RESOURCE);
            var inputBytes = Files.readAllBytes(file.toPath());
            var compressed = LZW.compress(inputBytes);
            var decompressed = LZW.decompress(compressed);

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
            var compressed = LZW.compress(inputBytes);
            var decompressed = LZW.decompress(compressed);

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
            var compressed = LZW.compress(inputBytes);
            var decompressed = LZW.decompress(compressed);

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
            var compressed = LZW.compress(inputBytes);

            file = new File(resource + ".wc");
            Files.write(file.toPath(), compressed);

            System.out.println("Input: " + inputBytes.length + " Output: " + compressed.length);
        } catch (Exception e) {
            fail();
        }
    }


    void decompress(){
        try {
            var resource = PDF_RESOURCE;
            File file = new File(resource + ".wc");
            byte[] fileContent = Files.readAllBytes(file.toPath());
            byte[] decompress = LZW.decompress(fileContent);

            file = new File(resource);
            Files.write(file.toPath(), decompress);

        } catch (IOException e) {
            fail();
        }
    }
}

