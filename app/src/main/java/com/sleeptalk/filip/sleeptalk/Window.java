package com.sleeptalk.filip.sleeptalk;

import java.util.List;

/**
 * Created by filip on 03.05.16.
 */
interface Window {
    public void build(int size);
    public List<Double> multiplyWithSignal(List<Double> signal);
}
