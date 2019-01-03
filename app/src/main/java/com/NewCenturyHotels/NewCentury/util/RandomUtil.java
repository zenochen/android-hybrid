package com.NewCenturyHotels.NewCentury.util;

import java.util.Random;

public class RandomUtil {

    public static int getRandomIndex(int min,int max){
        Random random = new Random();
        return random.nextInt(max-min) + min;
    }

    public static int getRandom(int current,int total){
        if(current >= total){
            return 0;
        }else{
            return ++current;
        }
    }
}
