package com.sleeptalk.filip.sleeptalk;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 02.05.16.
 */
public class FourierTransform {

    private static int fftMinimumPointsNumber = 2;

    public List<ComplexNumber> dft(List<Double> signal, int resolution){
        double angle;
        double nthSample;
        ComplexNumber outputSample;
        int sampleNumber = signal.size();
        List<ComplexNumber> result = new ArrayList<>();
        // First outputSample loop
        for(int k = 0; k < resolution; k++){
            // Multiply and accumulate, multiply signal sample with sine wave
            outputSample = new ComplexNumber(0, 0);
            for(int n = 0; n < sampleNumber; n++){
                nthSample = signal.get(n);
                angle = -2*Math.PI*k*n/sampleNumber;
                outputSample = ComplexNumber.add(
                        ComplexNumber.multiply(
                                new ComplexNumber(nthSample, 0),
                                ComplexNumber.exp(new ComplexNumber(0, angle))
                        ),
                        outputSample
                );
            }
            result.add(outputSample);
        }
        return result;
    }

    public List<ComplexNumber> fft(List<Double> signal){

        // Complex numbers
        ComplexNumber factor;
        ComplexNumber outputSample;
        ComplexNumber oddSample;
        ComplexNumber evenSample;
        // Complex Arrays
        List<ComplexNumber> oddDft;
        List<ComplexNumber> evenDft;
        List<ComplexNumber> output = new ArrayList<>();
        List<Double> oddNumbers = new ArrayList<>();
        List<Double> evenNumbers = new ArrayList<>();
        int N = signal.size();
        int halfN = N/2;

        // Illegal argument, Cooley-Turkey algorithm input signal length should be a power of 2
        if(N % 2 > 0){
            throw new IllegalArgumentException("size of signal must be a power of 2");
        }
        // DFT for small arrays
        else if(N <= fftMinimumPointsNumber){
            return dft(signal, N);
        }
        // If greater then 32 divide into two arrays with odd and even indexes
        else{
            for (int i = 0; i < N; i++){
                if(i % 2 == 0){
                    evenNumbers.add(signal.get(i));
                }
                else{
                    oddNumbers.add(signal.get(i));
                }
            }
            oddDft = fft(oddNumbers);
            evenDft = fft(evenNumbers);
            for(int k = 0; k < N; k++){
                factor = ComplexNumber.exp(new ComplexNumber(0, -2*Math.PI * k / N));
                evenSample = evenDft.get(k % halfN);
                oddSample = oddDft.get(k % halfN);
                outputSample = ComplexNumber.add(evenSample, ComplexNumber.multiply(factor, oddSample));
                output.add(outputSample);
            }
            return output;
        }

    }

    public static double nextPow(int signalSize){
        int counter = 0;
        double power;
        while(true){
            power = Math.pow(2, counter);
            if (power > signalSize){
                return power;
            }
            counter++;
        }
    }

    public static List<Double> addZeros(List<Double> signal){
        int nxtPower = (int) nextPow(signal.size());
        int diff = nxtPower - signal.size();
        for(int i = 0; i < diff; i++ ){
            signal.add(0.0);
        }
        return signal;
    }

}
