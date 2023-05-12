package com.lyadev.cookingrecipes.service;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.Deflater;

public class FileService {
    public static byte[] compressImage(byte[] data, String name) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] tmp = new byte[4*1024];
        while (!deflater.finished()){
            int size = deflater.deflate(tmp);
            outputStream.write(tmp,0, size);
        }
        try {
            outputStream.close();
        }catch (Exception e){
            System.out.println("File error " + e);
        }
        File file = new File(name);
        file.createNewFile();
        try (FileOutputStream fos = new FileOutputStream(name)) {
            fos.write(data);
            //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

}
