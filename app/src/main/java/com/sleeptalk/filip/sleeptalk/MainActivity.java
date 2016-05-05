package com.sleeptalk.filip.sleeptalk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FileSaver datafile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android objects initialization
        Button recButton = (Button)findViewById(R.id.rec_button);
        datafile = new FileSaver(this);
//        WavFile file = new WavFile("/sdcard/square.wav");
        WavFile file = new WavFile("/sdcard/Music/FB_MAT_1.wav");

        List<Double> wavBuffer = file.read();
        Standarizer signalStandarizer = new Standarizer(wavBuffer, file.sampleRate);
        signalStandarizer.decimate(16000);
        signalStandarizer.standard();
        signalStandarizer.lfilter(Arrays.asList(signalStandarizer.highPassCoeffs));
        signalStandarizer.preemphasis();
        Pair<List<Double>,Integer> signalParams = signalStandarizer.getSignal();
        List<Double> stdSignal = signalParams.first;
        int sampleRate = signalParams.second;
        Mfcc mfcc = new Mfcc(stdSignal, sampleRate);
        List<List<Double>> mfccCoefs = mfcc.compute();



        /// FFT Tet
//        List<Double> nlist = new ArrayList<>();
//        for(int i = 0 ; i < 128; i++){
//            nlist.add(0.0);
//        }
//        List<Double> newSig = new ArrayList<>();
//        FourierTransform ft = new FourierTransform();
//        wavBuffer = wavBuffer.subList(0, 256);
//        wavBuffer.addAll(nlist);
//        List<ComplexNumber> psd = ft.fft(FourierTransform.addZeros(wavBuffer));
//        for(ComplexNumber cnp: psd){
//            newSig.add(Math.pow(ComplexNumber.abs(cnp), 2));
//        }
//
//        List<Double> testlist = wavBuffer;
//        for(int n = 0; n < testlist.size(); n++){
//            testlist.set(n, Math.pow(testlist.get(n),2 ));
//        }
//        double aa = Statistics.sum(testlist);
//        double bb = (1.0/newSig.size())*Statistics.sum(newSig);
//        newSig.size();
////
        try {
            datafile.save3D(mfccCoefs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // OnClick listeners
        recButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("debug", "Hello");
            }
        });
    }
}
