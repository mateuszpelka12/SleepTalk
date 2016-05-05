package com.sleeptalk.filip.sleeptalk;

import java.util.List;

/**
 * Created by filip on 03.05.16.
 * PL: Interfejs reprezentujący okno, wymnażane z sygnałem
 * zapobiega zjawisku "wyciekania" widma.
 */
interface Window {
    public void build(int size);
    public List<Double> multiplyWithSignal(List<Double> signal);
}
