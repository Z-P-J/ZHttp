package com.gym.easyokhttp.httpconnection;


import com.gym.easyokhttp.httpconnection.http.HttpMethod;
import com.gym.easyokhttp.httpconnection.http.HttpRequest;
import com.gym.easyokhttp.httpconnection.utils.Utills;

import java.io.IOException;
import java.net.URI;

/**
 * @function 封装请求HttpRequestProvider，供上层调用
 * @author lemon Guo
 */

public class HttpRequestProvider {

    private static boolean OKHTTP_REQUEST = Utills.isExist("okhttp3.OkHttpClient", HttpRequestProvider.class.getClassLoader());

    private HttpRequestFactory mHttpRequestFactory;

    public HttpRequestProvider() {
        if (OKHTTP_REQUEST) {
            mHttpRequestFactory = new OkHttpRequestFactory();
        } else {
//            mHttpRequestFactory = new OriginHttpRequestFactory();
        }
    }

    public HttpRequest getHttpRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return mHttpRequestFactory.createHttpRequest(uri, httpMethod);
    }

    public HttpRequestFactory getHttpRequestFactory() {
        return mHttpRequestFactory;
    }

    public void setHttpRequestFactory(HttpRequestFactory httpRequestFactory) {
        mHttpRequestFactory = httpRequestFactory;
    }
}
