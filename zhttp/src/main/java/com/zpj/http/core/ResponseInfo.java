package com.zpj.http.core;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseInfo {

    private int statusCode;
    private String statusMessage;
    private String contentType;
    private long contentLength;
    protected Map<String, String> headers;
    private Callback callback;

    public interface Callback {
        InputStream get() throws Exception;
    }

    private ResponseInfo() {

    }

    public static ResponseInfo build() {
        return new ResponseInfo();
    }

    public ResponseInfo onGetBodyStream(Callback callback) {
        this.callback = callback;
        return this;
    }

    public InputStream getBodyStream() throws Exception {
        if (callback != null) {
            return callback.get();
        }
        return null;
    }

    public ResponseInfo setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public ResponseInfo setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    public ResponseInfo setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public ResponseInfo setContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public ResponseInfo setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getContentType() {
        return contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return "ResponseInfo{" +
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentLength=" + contentLength +
                ", headers=" + headers +
                ", callback=" + callback +
                '}';
    }
}
