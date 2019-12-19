package com.gym.easyokhttp.httpconnection;

import com.gym.easyokhttp.httpconnection.http.HttpHeader;
import com.gym.easyokhttp.httpconnection.http.HttpMethod;
import com.gym.easyokhttp.httpconnection.http.HttpResponse;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @function 实现类OkHttpRequest
 * @author lemon guo
 */

public class OkHttpRequest extends BufferHttpRequest {

    private OkHttpClient mClient;

    private HttpMethod mMethod;

    private String mUrl;

    public OkHttpRequest(OkHttpClient client, HttpMethod method, String url) {
        this.mClient = client;
        this.mMethod = method;
        this.mUrl = url;
    }

    @Override
    protected HttpResponse executeInternal(HttpHeader header, byte[] data) throws IOException {
        boolean isBody = mMethod == HttpMethod.POST;
        RequestBody requestBody = null;
        if (isBody) {
            requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), data);
        }
        Request.Builder builder = new Request.Builder().url(mUrl).method(mMethod.name(), requestBody);

        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        Response response = mClient.newCall(builder.build()).execute();

        System.out.println("fuck "+response.body().contentLength());

        return new OkHttpResponse(response);
    }

    @Override
    public HttpMethod getMethod() {
        return mMethod;
    }

    @Override
    public URI getUri() {
        return URI.create(mUrl);
    }
}
