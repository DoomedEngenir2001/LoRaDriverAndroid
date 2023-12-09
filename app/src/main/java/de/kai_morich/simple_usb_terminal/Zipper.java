package de.kai_morich.simple_usb_terminal;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.zip.*;
import java.io.*;
public class Zipper {
    private static final int compressedSize =64;
    public static byte [] CompressdBuffer = new byte[compressedSize];

    public static byte[] flate(byte[] fileData){// сжать
        byte[] compressed = new byte[fileData.length];
        Deflater compressor = new Deflater();
        compressor.setInput(fileData);
        compressor.finish();
        int len = compressor.deflate(compressed);
        byte[] cutCompressed = Arrays.copyOf(compressed, len);
        compressor.end();
        return cutCompressed;
    }

    public static byte[] deflate(byte[] compressedData){// разжать
        try{
            byte[] decompressedData = new byte[100024];
            Inflater decompressor = new Inflater();
            decompressor.setInput(compressedData);
            int len = decompressor.inflate(decompressedData);
            byte[] cutData = Arrays.copyOf(decompressedData, len);
            decompressor.end();
            return  cutData;

        }catch (DataFormatException ex){
            System.out.println("Unknown extension");
            return new byte[1];
        }


    }

    public static void decompress(String filename, String zipFile){
        try{
            FileInputStream is = new FileInputStream(zipFile);
            FileOutputStream os = new FileOutputStream(filename);
            GZIPInputStream GzipInSteam = new GZIPInputStream(is);
            byte[] decompressdBuffer = new byte[compressedSize];
            int pos;
            while((pos = GzipInSteam.read(decompressdBuffer))!=-1){
                os.write(decompressdBuffer,0,pos);
            }
            GzipInSteam.close();
            os.close();
            is.close();

        }catch(IOException e){
            System.out.println(e);
        }


    }

}
