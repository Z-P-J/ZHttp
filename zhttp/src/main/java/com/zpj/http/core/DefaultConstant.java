package com.zpj.http.core;

import android.os.Environment;

/**
 * @author Z-P-J
 * */
public class DefaultConstant {

    public static final int BUFFER_SIZE = 1024;

    public static final int MAX_BODY_SIZE = 0;

    public static final String USER_AGENT = System.getProperty("http.agent");

    public static final int RETRY_COUNT = 3;

    public static final int MAX_REDIRECTS = 20;

    public static final CookieJar COOKIE_JAR = new DefaultCookieJar();

    // 单位毫秒
    public static final int RETRY_DELAY = 10 * 1000;

    public static final int CONNECT_OUT_TIME = 30000;
    public static final int READ_OUT_TIME = 30000;

    public static final String KEY_DOWNLOAD_PATH = "download_path";

    public static final String KEY_THREAD_COUNT = "thread_count";

    public static final String KEY_BLOCK_SIZE = "blockSize";

    public static final String KEY_USER_AGENT = "userAgent";

    public static final String KEY_RETRY_COUNT = "retry_count";

    public static final String KEY_RETRY_DELAY = "retry_delay";

    public static final String KEY_TONG_SHI = "tong_shi";

    private DefaultConstant() {

    }

}
