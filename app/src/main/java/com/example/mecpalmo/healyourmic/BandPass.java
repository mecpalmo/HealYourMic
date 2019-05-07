package com.example.mecpalmo.healyourmic;

public class BandPass {

    int amount = 12; //default value
    int[] borders;
    int n;

    public BandPass(){
        amount = 12;
        n = Global.n;
        createBorders();
    }

    public BandPass(int am,int _n){
        amount = am; //am has to be divideable by 2 or 3;
        n = _n;
        createBorders();
    }

    public int returnAmount(){
        return amount;
    }

    public int borderValue(int i){
        return borders[i];
    }

    private void createBorders(){
        borders = new int[amount+1];
        borders[0] = Global.getBottomFreqAddress();
        borders[amount] = Global.getTopFreqAddress();
        double TotalDifference = (double)Global.topFreq / (double)Global.bottomFreq;
        int CurrentAmount = amount;
        double SingleDifference = TotalDifference;
        while (CurrentAmount>1) {
            if(CurrentAmount % 3 == 0) {
                SingleDifference = Math.cbrt(SingleDifference);
                CurrentAmount = CurrentAmount/3;
            }
            if(CurrentAmount % 2 == 0){
                SingleDifference = Math.sqrt(SingleDifference);
                CurrentAmount = CurrentAmount/2;
            }
        }
        double CurrentFreq = Global.bottomFreq;
        for(int i=1; i<amount; i++){
            CurrentFreq = CurrentFreq*SingleDifference;
            borders[i] = (int)Math.round(n*CurrentFreq/Global.RECORDER_SAMPLERATE);
        }
    }
}
