package com.gym.easyokhttp.httpconnection.utils;

/**
 * @author nate
 */

public class Utills {


    public static boolean isExist(String className, ClassLoader loader) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
