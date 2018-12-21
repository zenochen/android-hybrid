package com.NewCenturyHotels.NewCentury.util;

import java.util.Random;

public class RandomUtil {

    public static int getRandomIndex(int min,int max){
        Random random = new Random();
        return random.nextInt(max-min) + min;
    }
}
