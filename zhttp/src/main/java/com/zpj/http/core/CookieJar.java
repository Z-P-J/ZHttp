package com.zpj.http.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.URL;
import java.util.List;
import java.util.Map;

// TODO Cookie管理
public interface CookieJar {

    @Nullable
    Map<String, String> loadCookies(@NonNull URL url);

    void saveCookies(@NonNull URL url, @NonNull Map<String, String> cookieMap);

}
