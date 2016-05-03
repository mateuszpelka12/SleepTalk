package com.sleeptalk.filip.sleeptalk;

import java.util.List;

/**
 * Created by filip on 03.05.16.
 */
public class Statistics {
    public static double mean(List<Double> list){
        double sum = 0;
        for(Double num: list){
            sum+=num;
        }
        return sum/ list.size();
    }
    public static double var(List<Double> list){
        double sum = 0;
        double listMean = mean(list);
        for(Double num: list){
            sum+= Math.pow(num - listMean, 2);
        }
        return sum/ list.size();
    }
}
