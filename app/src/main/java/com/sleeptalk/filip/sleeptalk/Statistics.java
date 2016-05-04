package com.sleeptalk.filip.sleeptalk;

import java.util.Collections;
import java.util.List;

/**
 * Created by filip on 03.05.16.
 */
public class Statistics {
    public static double mean(List<Double> list){
        return sum(list)/ list.size();
    }

    public static double sum(List<Double> list){
        double sum = 0;
        for(Double num: list){
            sum+=num;
        }
        return sum;
    }

    public static int isum(List<Integer> list){
        int sum = 0;
        for(Integer num: list){
            sum+=num;
        }
        return sum;
    }

    public static double var(List<Double> list){
        double varSum = 0;
        double listMean = mean(list);
        for(Double num: list){
            varSum+= Math.pow(num - listMean, 2);
        }
        return varSum/ list.size();
    }
    // Maximum value for double list
    public static double max(List<Double> signal){
        return Collections.max(signal);
    }
    // Maximum value for integer list
    public static int imax(List<Integer> signal){
        return Collections.max(signal);
    }
    public static int argmax(List<Double> signal){
        return signal.indexOf(max(signal));
    }
    public static int iargmax(List<Integer> signal){
        return signal.indexOf(imax(signal));
    }
}
