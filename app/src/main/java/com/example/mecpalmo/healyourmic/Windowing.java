package com.example.mecpalmo.healyourmic;

public class Windowing {

    public static double[] Hanning(double[] input){
        double[] output = new double[input.length];

        int N = input.length;

        for(int i=0;i<N;i++){
            output[i] = (0.5 - (0.5*Math.cos(2.0*Math.PI*(double)i/N))) * input[i];
        }

        return output;
    }

    public static double[] Hamming(double[] input){
        double[] output = new double[input.length];

        int N = input.length;

        for(int i=0;i<N;i++){
            output[i] = (0.54 - (0.46*Math.cos(2.0*Math.PI*(double)i/N))) * input[i];
        }

        return output;
    }

    public static double[] reverseHanning(double[] input){
        double[] output = new double[input.length];

        int N = input.length;

        for(int i=0;i<N;i++){
            output[i] = input[i] / (0.5 - (0.5*Math.cos(2.0*Math.PI*(double)i/N)));
        }

        return output;
    }

    public static double[] reverseHamming(double[] input){
        double[] output = new double[input.length];

        int N = input.length;

        for(int i=0;i<N;i++){
            output[i] = input[i] / (0.54 - (0.46*Math.cos(2.0*Math.PI*(double)i/N)));
        }

        return output;
    }
}
