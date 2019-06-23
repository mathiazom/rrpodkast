package com.rrpm.mzom.projectrrpm.utils;

public class MathUtils {

    private static int constrain(int number, int min, int max){

        return (number < min) ? min : ((number > max) ? max : number);

    }

    public static int constrainPositive(int number, int max){

        return constrain(number,0,max);

    }

    public static boolean isConstrained(int number, int min, int max){

        return min <= number && number <= max;

    }




}
