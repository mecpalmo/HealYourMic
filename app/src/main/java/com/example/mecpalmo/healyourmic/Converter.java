package com.example.mecpalmo.healyourmic;

public class Converter {

    public static short[] byteToShort(byte[] byteBuffer){
        short[] shortBuffer = new short[byteBuffer.length / 2];
        for (int i = 0; i < shortBuffer.length; i++) {
            shortBuffer[i] = (short)((byteBuffer[i * 2] & 0xff) | (byteBuffer[i * 2 + 1] << 8));
        }
        return shortBuffer;
    }

    public static byte[] shortToByte(short[] shortBuffer){
        byte[] byteBuffer = new byte[shortBuffer.length*2];
        for (int i=0; i<shortBuffer.length; i++){
            byteBuffer[2*i  ] = (byte)(shortBuffer[i] & 0x00FF);
            byteBuffer[2*i+1] = (byte)((shortBuffer[i] & 0xFF00) >> 8);
        }
        return byteBuffer;
    }

    public static double[] shortToDouble(short[] shortBuffer){
        double[] doubles = new double[shortBuffer.length];
        for(int i=0; i<shortBuffer.length; i++){
            doubles[i] = (double)shortBuffer[i] / 32768.0;
        }
        return doubles;
    }

    public static short[] doubleToShort(double[] doubleBuffer){
        short[] shorts = new short[doubleBuffer.length];
        for(int i=0; i<doubleBuffer.length; i++) {
            if (doubleBuffer[i] * 32768.0 > Short.MAX_VALUE) {
                shorts[i] = Short.MAX_VALUE;
            } else if (doubleBuffer[i] * 32768.0 < Short.MIN_VALUE) {
                shorts[i] = Short.MIN_VALUE;
            } else {
                shorts[i] = (short)(doubleBuffer[i] * 32768.0);
            }
        }
        return shorts;
    }

    public static Complex[] doubleToComplex(double[] doubleBuffer){
        Complex[] complexes = new Complex[doubleBuffer.length];
        for(int i=0; i<doubleBuffer.length; i++){
            complexes[i] = new Complex(doubleBuffer[i],0);
        }
        return complexes;
    }

    public static double[] complexToDoubleAbs(Complex[] complexBuffer){
        double[] doubles = new double[complexBuffer.length];
        for(int i=0; i<complexBuffer.length; i++){
            doubles[i] = complexBuffer[i].abs();
        }
        return doubles;
    }

    public static double[] complexToDoubleRe(Complex[] complexBuffer){
        double[] doubles = new double[complexBuffer.length];
        for(int i=0; i<complexBuffer.length; i++){
            doubles[i] = complexBuffer[i].re();
        }
        return doubles;
    }

    public static double getAverage(double[] doubleBuffer){
        long length = doubleBuffer.length;
        double sum = 0;
        for(int i=0; i<length; i++){
            sum = sum + doubleBuffer[i]/length;
        }
        return sum;
    }

    public static double getMaxValue(double[] doubleBuffer){
        int length = doubleBuffer.length;
        double max = doubleBuffer[0];
        for(int i=1; i<length; i++){
            if(doubleBuffer[i]>max){
                max = doubleBuffer[i];
            }
        }
        return max;
    }

    public static double getMinValue(double[] doubleBuffer){
        int length = doubleBuffer.length;
        double min = doubleBuffer[0];
        for(int i=1; i<length; i++){
            if(doubleBuffer[i]<min){
                min = doubleBuffer[i];
            }
        }
        return min;
    }

    public static int getMaxAddress(double[] doubleBuffer){
        int length = doubleBuffer.length;
        int max = 0;
        for(int i=1; i<length; i++){
            if(doubleBuffer[i]>max){
                max = i;
            }
        }
        return max;
    }

    public static int getMinAddress(double[] doubleBuffer){
        int length = doubleBuffer.length;
        int min = 0;
        for(int i=1; i<length; i++){
            if(doubleBuffer[i]<min){
                min = i;
            }
        }
        return min;
    }

    public static double[] powLinearToLog(double[] doubleBuffer){
        int length = doubleBuffer.length;
        double[] log = new double[length];
        for(int i=0; i<length; i++){
            log[i]=10*Math.log10(doubleBuffer[i]);
        }
        return log;
    }

    public static double[] powLogToLinear(double[] doubleBuffer){
        int length = doubleBuffer.length;
        double[] lin = new double[length];
        for(int i=0; i<length; i++){
            lin[i]=Math.pow(10,doubleBuffer[i]/10);
        }
        return lin;
    }

    public static double powLinearToLog(double doubleBuffer){
        return 10*Math.log10(doubleBuffer);
    }

    public static double powLogToLinear(double doubleBuffer){
        return Math.pow(10,doubleBuffer/10);
    }

    public static double[] ampLinearToLog(double[] doubleBuffer){
        int length = doubleBuffer.length;
        double[] log = new double[length];
        for(int i=0; i<length; i++){
            log[i]=20*Math.log10(doubleBuffer[i]);
        }
        return log;
    }

    public static double[] ampLogToLinear(double[] doubleBuffer){
        int length = doubleBuffer.length;
        double[] lin = new double[length];
        for(int i=0; i<length; i++){
            lin[i]=Math.pow(10,doubleBuffer[i]/20);
        }
        return lin;
    }

    public static double ampLinearToLog(double doubleBuffer){
        return 20*Math.log10(doubleBuffer);
    }

    public static double ampLogToLinear(double doubleBuffer){
        return Math.pow(10,doubleBuffer/20);
    }

    public static double[] scale(double[] input, double factor){
        double[] output = new double[input.length];
        for(int i=0;i<input.length;i++){
            output[i] = input[i] * factor;
        }
        return output;
    }
}
