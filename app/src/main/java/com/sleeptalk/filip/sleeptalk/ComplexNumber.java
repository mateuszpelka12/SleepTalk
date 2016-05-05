package com.sleeptalk.filip.sleeptalk;

/**
 * Created by filip on 02.05.16.
 * PL : Instancja klasy reprezentuje liczbę zespolona,
 * Posiada również metody statyczne umożliwiające podstawowe operacje na liczbach
 * zespolonych.
 */
public class ComplexNumber {

    double real;
    double img;

    public ComplexNumber(double real, double complex) {
        this.real = real;
        this.img = complex;
    }

    public static ComplexNumber add(ComplexNumber first, ComplexNumber next){
        return new ComplexNumber(first.real + next.real, first.img + next.img);
    }

    public static ComplexNumber exp(ComplexNumber arg) {
        double r = Math.exp(arg.real);
        return new ComplexNumber(r * Math.cos(arg.img), r * Math.sin(arg.img));
    }

    public static ComplexNumber multiply(ComplexNumber first, ComplexNumber next){
        double newReal = first.real*next.real - first.img*next.img;
        double newImag = first.real*next.img + first.img*next.real;
        return new ComplexNumber(newReal, newImag);
    }

    public static double imag(ComplexNumber z){
        return z.img;
    }

    public static double real(ComplexNumber z){
        return z.real;
    }

    public static double abs(ComplexNumber z){
        return Math.sqrt(Math.pow(z.real, 2) + Math.pow(z.img,2));
    }

    public String toString() {
        return this.real + " + " + this.img + "j";
    }
}