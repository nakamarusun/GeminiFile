package com.geminifile.core;

import java.util.Random;

public class MathUtil {

    public static int randomRange(int min, int max) {
        Random rand = new Random();
        return (rand.nextInt(max - min + 1) + min);
    }

    public static String generateRandomAlphaNum(int size) {
        StringBuilder str = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            switch (rand.nextInt(3)) {
                case 0:
                    str.append( (char)randomRange(48, 57) );
                    break;
                case 1:
                    str.append( (char)randomRange(65, 90) );
                    break;
                case 2:
                    str.append( (char)randomRange(97, 122) );
                    break;
            }
        }
        return str.toString();
    }

}
