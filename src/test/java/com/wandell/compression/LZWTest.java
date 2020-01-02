package com.wandell.compression;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class LZWTest {
    @Test
    void testCompression() {
        try {
            StringBuilder contentBuilder = new StringBuilder();

            try (Stream<String> stream = Files.lines(Paths.get("bible.txt"))) {
                stream.forEach(s -> contentBuilder.append(s).append("\n"));
            }

            byte[] compressed = LZW.compress(contentBuilder.toString());
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

