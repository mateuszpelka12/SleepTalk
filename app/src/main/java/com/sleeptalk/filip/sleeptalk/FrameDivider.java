package com.sleeptalk.filip.sleeptalk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 03.05.16.
 */
public class FrameDivider {

    private List<Double> signal;
    private int sampleRate;

    public FrameDivider(List<Double> signal, int sampleRate){
        this.signal = signal;
        this.sampleRate = sampleRate;
    }

    private void appendZeros(int zerosNumber){
        for(int i = 0; i < zerosNumber; i++){
            signal.add(0.0);
        }
    }

    public List<List<Double>> divide(double frameTime, double timeOverlap){
        int startFrame;
        int endFrame;
        int signalSize = signal.size();
        int frameSamples = (int) (frameTime*sampleRate);
        int overlapSamples = (int) (timeOverlap*sampleRate);
        int stepNumber = signalSize / overlapSamples;

        List<Double> frame = new ArrayList<>();
        List<List<Double>> framesBuffer = new ArrayList<>();

        // Add zeros to signal
        appendZeros(frameSamples);
        for(int counter = 0; counter < stepNumber; counter++){
            startFrame = counter * overlapSamples;
            endFrame = startFrame + frameSamples;
            frame = signal.subList(startFrame, endFrame);
            framesBuffer.add(frame);
        }
        return framesBuffer;
    }
}
