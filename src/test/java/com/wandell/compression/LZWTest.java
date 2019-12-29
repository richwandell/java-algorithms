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

            try (Stream<String> stream = Files.lines(Paths.get("source.txt"))) {
                stream.forEach(s -> contentBuilder.append(s).append("\n"));
            }

            byte[] compressed = LZW.compress(contentBuilder.toString());
            File file = new File("dest.txt");
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
            File file = new File("dest.txt");
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String decompress = LZW.decompress(fileContent);
            System.out.println(decompress);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

//package com.wandell.PHP;
//
//        import com.wandell.PHP.pojo.Pojo1;
//        import com.wandell.PHP.pojo.Pojo2;
//        import org.junit.jupiter.api.Test;
//        import static org.junit.jupiter.api.Assertions.*;
//
//public class SerializerTest {
//
//    @Test
//    void testSimpleSerialize() {
//        Pojo1 pojo1 = new Pojo1();
//
//        try {
//            Serializer serializer = new Serializer();
//            String serialized = serializer.serialize(pojo1);
//
//            System.out.println(serialized);
//        } catch (Exception.MissingPHPClassAnnotation missingPHPClassAnnotation) {
//            fail();
//        }
//    }
//
//    @Test
//    void testSerializeObjectWithReferences() {
//        Pojo1 pojo1 = new Pojo1();
//        Pojo2 pojo2 = new Pojo2(pojo1, pojo1);
//
//
//        try {
//            Serializer serializer = new Serializer();
//            String serialized = serializer.serialize(pojo2);
//
//            System.out.println(serialized);
//        } catch (Exception.MissingPHPClassAnnotation missingPHPClassAnnotation) {
//            fail();
//        }
//    }
//}

