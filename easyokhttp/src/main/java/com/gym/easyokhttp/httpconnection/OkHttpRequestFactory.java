package com.gym.easyokhttp.httpconnection;

import com.gym.easyokhttp.httpconnection.http.HttpMethod;
import com.gym.easyokhttp.httpconnection.http.HttpRequest;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @function 实现类OkHttpRequestFactory（返回HttpRequest对象）
 * @author lemon Guo
 */

public class OkHttpRequestFactory implements HttpRequestFactory {

    private OkHttpClient mClient;

    public OkHttpRequestFactory() {
        this.mClient = new OkHttpClient();
    }

    public OkHttpRequestFactory(OkHttpClient client) {
        this.mClient = client;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.mClient = mClient.newBuilder().
                readTimeout(readTimeOut, TimeUnit.MILLISECONDS).
                build();
    }

    public void setWriteTimeOut(int writeTimeOut) {
        this.mClient = mClient.newBuilder().
                writeTimeout(writeTimeOut, TimeUnit.MILLISECONDS).
                build();
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.mClient = mClient.newBuilder().
                connectTimeout(connectionTimeOut, TimeUnit.MILLISECONDS).
                build();
    }

    @Override
    public HttpRequest createHttpRequest(URI uri, HttpMethod method) {
        return new OkHttpRequest(mClient, method, uri.toString());
    }
}
