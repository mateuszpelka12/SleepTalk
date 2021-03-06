package com.sleeptalk.filip.sleeptalk;

import android.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 03.05.16.
 * PL: Autorska implementacja VAD.
 * Jej celem jest usuwanie ciszy z sygnału.
 */
public class VoiceDetector {

    // Private variables
    private int sampleRate;
    private List<Double> signal;
    private static double FrameTime = 0.025;
    private static double FrameOverlap = 0.015;
    private static double distanceFromMax = 0.33;

    // Public
    public Hamming window;
    public List<List<Double>> framesBuffer;
    public List<List<ComplexNumber>> framesPSDBuffer;

    public VoiceDetector(List<Double> signal, int sampleRate){
        this.signal = signal;
        window = new Hamming();
        this.sampleRate = sampleRate;
        this.framesPSDBuffer = new ArrayList<>();
        FrameDivider signalFraming = new FrameDivider(signal, sampleRate);
        this.framesBuffer = signalFraming.divide(FrameTime, FrameOverlap);
    }

    public static Pair<Integer, Integer> cutSig(List<Integer> signal){
        List<Integer> changeIndex = new ArrayList<>();
        List<Integer> areaSum = new ArrayList<>();
        // Primitive types
        double maxValue;
        double maxIndex;
        int signalSize = signal.size();
        int distBackward;
        int distForward;
        int state = 0;
        int np;
        int nk;

        // Find points where state was changed
        for(int n = 0; n < signalSize; n++){
            if(signal.get(n) != state){
                state = signal.get(n);
                changeIndex.add(n);
            }
        }

        // If no noise exist
        if(changeIndex.size() == 2){
            new Pair<>(changeIndex.get(0), changeIndex.get(1));
        }

        // How big this area is
        for(int area = 0; area < changeIndex.size(); area = area + 2){
            areaSum.add(signal.subList(changeIndex.get(area), changeIndex.get(area + 1)).size());
        }

        // Find max values
        maxValue = Statistics.imax(areaSum);
        maxIndex = Statistics.iargmax(areaSum);
        np = (int) maxIndex * 2;
        nk = np + 1;

        // GO Backward
        if(maxIndex != 0){
            distBackward = signal.subList(changeIndex.get(np - 1) , changeIndex.get(np)).size();
            // Insert zeros in  noise area
            if(distBackward < distanceFromMax * maxValue){
                np = np - 1;
            }
        }

        // GO Forward
        if(maxIndex < areaSum.size() - 1){
            distForward = signal.subList(changeIndex.get(nk) , changeIndex.get(nk + 1)).size();
            // Insert zeros in  noise area
            if(distForward < distanceFromMax * maxValue){
                nk = nk + 2;
            }
        }
        return new Pair<>(changeIndex.get(np), changeIndex.get(nk));
    }

    // Find Zero Crossing Rate
    public static int findZeroCrossingRate(List<Double> signal){
        double signalSample;
        double sampleP1;
        double sampleP2;
        double sigRef;
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
    public List<Integer> humanInner(List<Integer> signal){
        int sample;
        int sampleP1;
        int sampleP2;
        int sampleM1;
        int sampleM2;
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
                signal.set(n, 0);
            }
            if(sample == 0 && ( sampleM1 == 1 || sampleM2 == 1) && ( sampleP1 == 1 || sampleP2 == 1)){
                signal.set(n, 1);
            }
        }
        return signal;
    }

    // Detect signal and remove silence from it
    public List<Double> removeSilence(){

        //Primitive types
        double alpha;
        double scale;
        double sampMeas;
        double framePower;
        double frameZCR;
        int firstIndex;
        int secondIndex;
        int counter = 0;
        double trigger = 0;

        List<Double> frame;
        List<ComplexNumber> framePSD;
        List<Double> weight = new ArrayList<>();
        List<Integer> signalBinary = new ArrayList<>();
        Hamming window = new Hamming();
        FourierTransform fft = new FourierTransform();
        int frameLength = framesBuffer.get(0).size();
        // Build Hamming window
        window.build(frameLength);
        // Loop over frames
        for(List<Double> currentFrame: framesBuffer){
            frame = window.multiplyWithSignal(currentFrame);

            // Calculate signal PSD
            framePSD = fft.fft(FourierTransform.addZeros(frame));
            framesPSDBuffer.add(framePSD);
            framePower = 0;
            for (ComplexNumber cnb: framePSD){
                framePower += Math.pow(ComplexNumber.abs(cnb), 2);
            }

            // Find frame features
            framePower = (1.0 / frameLength)*framePower;
            frameZCR = (1.0 / frameLength)*findZeroCrossingRate(frame);

            // Collect 10 frames
            if(counter < 10){
                weight.add(framePower*( 1 - frameZCR) * 1000);
            }
            else if(counter == 10){
                alpha = Math.pow(0.01 * Statistics.var(weight), (-0.92));
                trigger = Statistics.mean(weight) + alpha*Statistics.var(weight);
            }
            else{
                sampMeas = framePower * (1 - frameZCR) * 1000;
                if(sampMeas >= trigger){
                    signalBinary.add(1);
                }
                else{
                    signalBinary.add(0);
                }
            }

            counter++;
        }
        signalBinary = humanInner(signalBinary);
        Pair<Integer, Integer> points =  cutSig(signalBinary);
        scale = (signal.size() / signalBinary.size());
        firstIndex = (int) (points.first * scale);
        secondIndex = (int) (points.second * scale);
        framesBuffer = framesBuffer.subList(points.first + 11, points.second + 11);
        framesPSDBuffer = framesPSDBuffer.subList(points.first + 11, points.second + 11);
        return signal.subList(firstIndex, secondIndex);
    }

}
