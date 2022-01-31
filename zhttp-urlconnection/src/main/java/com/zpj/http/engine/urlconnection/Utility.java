package com.zpj.http.engine.urlconnection;

import java.util.Random;

public class Utility {

    private static final char[] MIME_BOUNDARY_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int BOUNDARY_LENGTH = 32;

    /**
     * Creates a random string, suitable for use as a mime boundary
     */
    public static String mimeBoundary() {
        final StringBuilder mime = new StringBuilder();
        final Random rand = new Random();
        for (int i = 0; i < BOUNDARY_LENGTH; i++) {
            mime.append(MIME_BOUNDARY_CHARS[rand.nextInt(MIME_BOUNDARY_CHARS.length)]);
        }
        return mime.toString();
    }

}
