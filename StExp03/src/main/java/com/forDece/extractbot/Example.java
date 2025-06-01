package com.forDece.extractbot;

public class Example {
    
    public static void example1(int[] numbers) 
    {
        int length = numbers.length;
        double med, var, sd, mean, sum, varsum;

        sum = 0;
        for(int i = 0; i < length; i++) 
        {
            sum += numbers[i];
        }
        med = numbers[length / 2];
        mean = sum / (double) length;

        varsum = 0;
        for(int i = 0; i < length; i++) 
        {
            varsum = varsum + ((numbers[i] - mean) * (numbers[i] - mean));
        }
        var = varsum / (length - 1.0);
        sd = Math.sqrt(var);

        System.out.println("length:              " + length);
        System.out.println("mean:                " + mean);
        System.out.println("median:              " + med);
        System.out.println("variance:            " + var);
        System.out.println("standard deviation:  " + sd);
    }
}


