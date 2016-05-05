package com.sleeptalk.filip.sleeptalk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 28.04.16.
 */
public class Mfcc{

    // Primitive types
    private int filters;
    private int sampleRate;
    private double highFilterBankFreq;
    private double lowFilterBankFreq;

    private Hamming window;
    private List<Double> signal;
    private List<List<Double>> framesBuffer;
    private List<List<ComplexNumber>> framesPSDBuffer;
    private static int defaultFilters = 10;

    public Mfcc(List<Double> signal, int sampleRate){
        this(signal, sampleRate, defaultFilters);
    }

    // Store signal and sample rate, extract frame buffer and psd from vad
    public Mfcc(List<Double> signal, int sampleRate, int filters){
        VoiceDetector vad = new VoiceDetector(signal, sampleRate);
        this.signal = vad.removeSilence();
        this.filters = filters;
        this.sampleRate = sampleRate;
        window = vad.window;
        framesPSDBuffer = vad.framesPSDBuffer;
        framesBuffer = vad.framesBuffer;
        lowFilterBankFreq = 300;
        highFilterBankFreq = sampleRate/2;
    }


    public void compute(){
        List<ComplexNumber> framePSD;
        melFilterBank(framesPSDBuffer.get(0).size());
        for(List<ComplexNumber> currentPSD: framesPSDBuffer) {
            framePSD = currentPSD.subList(0, currentPSD.size() / 2);

        }
    }

    // Build triangle filterbank to model
    // masking phenomenon
    public List<List<Double>> melFilterBank(int nfft, double lowFrequency, double highFrequency){
        List<List<Double>> melFilterBankBuffer = new ArrayList<>();
        double melFrequencies[] = new double[filters + 2];
        double hzFrequencies[] = new double[filters + 2];
        int frequencySamples[] = new int[filters + 2];
        double lowMelFrequency = hertz2Mel(lowFrequency);
        double highMelFrequency = hertz2Mel(highFrequency);
        double frequencyStep = ((highMelFrequency - lowMelFrequency) / (filters + 1));
        double frequency = lowMelFrequency;
        int psdLength = (nfft - 1) / 2 + 1;
        int counter = 1;

        // Initialize first and last frequencies
        melFrequencies[0] = lowMelFrequency;
        melFrequencies[filters + 1] = highMelFrequency;
        hzFrequencies[0] = mel2Hertz(lowMelFrequency);
        hzFrequencies[filters + 1] = mel2Hertz(highMelFrequency);
        frequencySamples[0] = (int) ((nfft + 1) * hzFrequencies[0] / sampleRate);
        frequencySamples[filters + 1] = (int) ((nfft + 1) * hzFrequencies[filters + 1] / sampleRate);

        // Add points in the middle
        while(frequency < highMelFrequency){
            frequency = frequency + frequencyStep;
            melFrequencies[counter] = frequency;
            hzFrequencies[counter] = mel2Hertz(frequency);
            frequencySamples[counter] = (int) ((nfft + 1) * hzFrequencies[counter] / sampleRate);
            counter++;
        }

        // Build filters
        for(int i = 1; i < filters + 1; i++){
            List<Double> melFilter = new ArrayList<>();
            for(int j = 1; j < psdLength; j++){
                if(j < frequencySamples[i-1]){
                    melFilter.add(0.0);
                }
                else if(frequencySamples[i-1] <= j && j<= frequencySamples[i]){
                    melFilter.add(((j - frequencySamples[i - 1]) * 1.0
                            / (frequencySamples[i] - frequencySamples[i - 1]) * 1.0));
                }
                else if(frequencySamples[i] <= j && j <= frequencySamples[i + 1]){
                    melFilter.add(((-j + frequencySamples[i+1]) * 1.0
                            /(frequencySamples[i+1] - frequencySamples[i]) * 1.0));
                }
                else{
                    melFilter.add(0.0);
                }
            }
            melFilterBankBuffer.add(melFilter);
        }

        return melFilterBankBuffer;
    }

    public List<List<Double>> melFilterBank(int psdLength){
        return melFilterBank(psdLength, lowFilterBankFreq, highFilterBankFreq);
    }

    // Convert Hertz frequencies to mel scale
    public static double hertz2Mel(double frequencyHertz){
        return 1125 * Math.log(1 + frequencyHertz / 700.0);
    }

    // Convert Mel scale frequency to scale in Hertz
    public static double mel2Hertz(double frequencyMel){
        return 700 * (Math.exp(frequencyMel / 1125.0) - 1);
    }

}
