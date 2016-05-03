package com.sleeptalk.filip.sleeptalk;

import java.util.List;

/**
 * Created by filip on 03.05.16.
 */
public class VoiceDetector {

    private int sampleRate;
    private List<Double> signal;
    private List<List<Double>> framesBuffer;
    private static double FrameTime = 0.025;
    private static double FrameOverlap = 0.010;

    public VoiceDetector(List<Double> signal, int sampleRate){
        this.signal = signal;
        this.sampleRate = sampleRate;
        FrameDivider signalFraming = new FrameDivider(signal, sampleRate);
        this.framesBuffer = signalFraming.divide(FrameTime, FrameOverlap);
    }

    // Find Zero Crossing Rate
    public static int findZeroCrossingRate(List<Double> signal){
        double signalSample;
        double sampleP1;
        double sampleP2;
        double sigRef = 0;
        int zerosCount = 0;
        int signalSize = signal.size();
        // Loop over samples
        for(int i = 0; i < signalSize - 2; i++){
            signalSample = signal.get(i);
            sampleP1 = signal.get(i + 1);
            sampleP2 = signal.get(i + 2);
            // If next sample is zero
            if(sampleP1 == 0){
                sigRef = sampleP2;
            }
            else{
                sigRef = sampleP1;
            }
            // Count zero crossing
            if(Math.signum(signalSample) == -Math.signum(sigRef))
                zerosCount = zerosCount + 1;
        }
        return zerosCount;
    }

    public void removeSilence(){
        int nFames = framesBuffer.size();

    }


}
