package com.wandell.compression;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.wandell.compression.Utils.getNumBits;
import static org.junit.jupiter.api.Assertions.*;

public class LZWTest {
    @Test
    void testCompression() {
        try {
            File fileIn = new File("bible.txt");
            byte[] inputBytes = Files.readAllBytes(fileIn.toPath());
            byte[] compressed = LZW.compress(inputBytes);
            File file = new File("test.txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(compressed);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testDecompression(){
        try {
            File file = new File("test.txt");
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String decompress = LZW.decompress(fileContent);

            file = new File("test.out.txt");
            Files.write(file.toPath(), decompress.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

