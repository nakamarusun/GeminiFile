package com.geminifile.core;

import java.io.File;
import java.util.Random;

public class MathUtil {

    public static int randomRange(int min, int max) {
        // Gets a random integer from min to max
        Random rand = new Random();
        return (rand.nextInt(max - min + 1) + min);
    }

    public static boolean randomBoolean() {
        Random rand = new Random();
        return rand.nextBoolean();
    }

    public static String generateRandomAlphaNum(int length) {
        // Generates an alpha numeric string with specified length
        StringBuilder str = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
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

    public static String fileSeparatorToOS(String filePath) {
        // Changes a file string's separator to the current machine's special file separator
        StringBuilder finalStr = new StringBuilder();

        String[] separated;

        if (filePath.contains("/")) {
            // UNIX
            separated = filePath.split("/");
        } else {
            // WINDOWS
            separated = filePath.split("\\\\");
        }

        // Do not add a separator a the end.
        for (String e : separated) {
            finalStr.append(e).append(File.separator);
        }
        finalStr.deleteCharAt(finalStr.length() - 1); // Deletes the separator at the end

        return finalStr.toString();
    }

}
