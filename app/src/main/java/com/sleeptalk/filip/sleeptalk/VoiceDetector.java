package com.sleeptalk.filip.sleeptalk;

import android.util.Log;

import java.util.ArrayList;
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

    // Human voice inertion
    public List<Double> humanInner(List<Double> signal){
        Double sample;
        Double sampleP1;
        Double sampleP2;
        Double sampleM1;
        Double sampleM2;
        int signalSize = signal.size();
        for (int n = 2; n < signalSize - 2; n++){
            // Get samples
            sample = signal.get(n);
            sampleP1 = signal.get(n + 1);
            sampleP2 = signal.get(n + 2);
            sampleM1 = signal.get(n - 1);
            sampleM2 = signal.get(n - 2);
            // Check neighbours
            if(sample == 1 && ( sampleM1 != 1 || sampleM2 != 1) && ( sampleP1 != 1 || sampleP2 != 1)){
                signal.set(n, 0.0);
            }
            if(sample == 0 && ( sampleM1 == 1 || sampleM2 == 1) && ( sampleP1 == 1 || sampleP2 == 1)){
                signal.set(n, 1.0);
            }
        }
        return signal;
    }

    // Detect signal and remove silence from it
    public List<Double> removeSilence(){
        List<Double> frame;
        List<Double> weight = new ArrayList<>();
        List<Double> signalBinary = new ArrayList<>();
        List<ComplexNumber> framePSD = new ArrayList<>();
        Hamming window = new Hamming();
        FourierTransform fft = new FourierTransform();
        //Primitive types
        double alpha;
        double sampMeas;
        double framePower;
        double frameZCR;
        int counter = 0;
        double trigger = 0;
        int nFames = framesBuffer.size();
        int frameLength = framesBuffer.get(0).size();
        // Build Hamming window
        window.build(frameLength);
        // Loop over frames
        for(List<Double> currentFrame: framesBuffer){
            frame = window.multiplyWithSignal(currentFrame);
            // Calculate signal PSD
            framePSD = fft.fft(FourierTransform.addZeros(frame));
            framePower = 0;
            for (ComplexNumber cnb: framePSD){
                framePower += Math.pow(ComplexNumber.abs(cnb), 2);
            }
            framePower = (1.0 / frameLength)*framePower;
            frameZCR = (1.0 / frameLength)*findZeroCrossingRate(frame);
            if(counter < 10){
                weight.add(framePower*( 1 - frameZCR) * 1000);
            }
            else if(counter == 10){
                alpha = Math.pow(0.01 * Statistics.var(weight), (-0.92));
                trigger = Statistics.mean(weight) + alpha*Statistics.var(weight);
            }
            else{
                sampMeas = framePower * ( 1 - frameZCR) * 1000;
                if(sampMeas >= trigger){
                    signalBinary.add(1.0);
                }
                else{
                    signalBinary.add(0.0);
                }
            }

            counter++;
        }
        signalBinary = humanInner(signalBinary);
        return signalBinary;
    }


}
