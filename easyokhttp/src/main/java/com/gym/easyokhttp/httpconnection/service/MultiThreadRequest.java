package com.gym.easyokhttp.httpconnection.service;

import com.gym.easyokhttp.httpconnection.http.HttpMethod;

/**
 * @funtion 业务层多线程分发处理，队列中的Request对象MultiThreadRequest
 * @author lemon Guo
 */

public class MultiThreadRequest {

    private String mUrl;

    private HttpMethod mMethod;

    private byte[] mData;

    private MultiThreadResponse mResponse;

    private String mContentType;

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public HttpMethod getMethod() {
        return mMethod;
    }

    public void setMethod(HttpMethod method) {
        mMethod = method;
    }

    public byte[] getData() {
        return mData;
    }

    public void setData(byte[] data) {
        mData = data;
    }

    public MultiThreadResponse getResponse() {
        return mResponse;
    }

    public void setResponse(MultiThreadResponse response) {
        mResponse = response;
    }

    public String getContentType() {
        return mContentType;
    }

    public void setContentType(String contentType) {
        mContentType = contentType;
    }
}

