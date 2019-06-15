package com.rrpm.mzom.projectrrpm.utils;

public class MathUtils {

    public static int constrain(int number, int min, int max){

        return (number < min) ? min : ((number > max) ? max : number);

    }

    public static boolean isConstrained(int number, int min, int max){

        return min <= number && number <= max;

    }




}
