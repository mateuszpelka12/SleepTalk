package com.sleeptalk.filip.sleeptalk;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 28.04.16.
 */
public class WavFile {

    private DataInputStream wavFile;

    // Microsoft wave header
    public String chunkID;
    public int chunkSize;
    public String format;
    public String subchunk1ID;
    public int subchunk1Size;
    public int audioFormat;
    public int numChannels;
    public int sampleRate;
    public int byteRate;
    public int blockAlign;
    public int bitsPerSample;
    public String subchunk2ID;

    // Wav File Constructor
    public WavFile(String filePath){
        try {
            InputStream is  = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(is, 8000);
            wavFile = new DataInputStream(bis);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Read 4 Bytes String from WaveHeader
    public String read4BytesString() throws IOException {

        byte byteArray[] = new byte[4];

        // Read ChunkId
        for(int i=0; i < 4; i++ ){
            byteArray[i] = wavFile.readByte();
        }
        return new String(byteArray);

    }

    // Read 4 bytes int
    public int read4BytesInt() throws IOException {

        byte byteArray[] = new byte[4];

        // Read ChunkId
        for(int i=0; i < 4; i++ ){
            byteArray[i] = wavFile.readByte();
        }

        return ((byteArray[3] & 0xFF) << 24 | (byteArray[2] & 0xFF) << 16 |
                (byteArray[1] & 0xFF) << 8 | (byteArray[0] & 0xFF));
    }

    // Read 2 bytes int
    public int read2BytesInt() throws IOException {

        byte byteArray[] = new byte[2];

        // Read ChunkId
        for(int i=0; i < 2; i++ ){
            byteArray[i] = wavFile.readByte();
        }
        return (byteArray[1]  << 8 | byteArray[0]) & 0xFFFF;
    }

    // Read Wave File Header
    public void wavHeaderInfo(){
        try {
            chunkID = read4BytesString();
            chunkSize = read4BytesInt();
            format = read4BytesString();
            subchunk1ID = read4BytesString();
            subchunk1Size = read4BytesInt();
            audioFormat = read2BytesInt();
            numChannels = read2BytesInt();
            sampleRate = read4BytesInt();
            byteRate = read4BytesInt();
            blockAlign = read2BytesInt();
            bitsPerSample = read2BytesInt();
            for(int i = 0; i < subchunk1Size - 16; i++)
                wavFile.readByte();
            subchunk2ID = read4BytesString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Double> readMono(){
        byte upper;
        byte lower;
        List<Double> wavBuffer = new ArrayList<>();
        // Read content from wav file
        try {
            while(wavFile.available() > 0){

                lower = wavFile.readByte();
                upper = wavFile.readByte();
                wavBuffer.add((double)(upper << 8 | lower & 0xFF) / 32768.0 );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wavBuffer;
    }

    public List<Double> readStereo(){
        byte upper;
        byte lower;
        double firstChannel;
        double secondChannel;
        List<Double> wavBuffer = new ArrayList<>();
        // Read content from wav file
        try {
            while(wavFile.available() > 0){

                // First Chanel sample
                lower = wavFile.readByte();
                upper = wavFile.readByte();
                firstChannel = (double)(upper << 8 | lower & 0xFF) / 32768.0;
                // Second Channel sample
                lower = wavFile.readByte();
                upper = wavFile.readByte();
                secondChannel = (double)(upper << 8 | lower & 0xFF) / 32768.0;
                wavBuffer.add((firstChannel + secondChannel) / 2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wavBuffer;
    }

    // Read wave file
    public List<Double> read(){
        List<Double> wavBuffer;
        // Read header
        wavHeaderInfo();
        if(numChannels == 1){
            wavBuffer = readMono();
        }
        else{
            wavBuffer = readStereo();
        }
        return wavBuffer;
    }

}
