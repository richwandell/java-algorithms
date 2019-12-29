package com.wandell.compression;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class LZWTest {
    private static final String TEXT1 = "aababbabbaaba";

    private static final String TEXT2 = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.";

    @Test
    void testCompression() {
        try {
            PrintWriter out = new PrintWriter("source.txt");
            out.write(TEXT2);
            out.close();

            byte[] compressed = LZW.compress(TEXT2);
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

