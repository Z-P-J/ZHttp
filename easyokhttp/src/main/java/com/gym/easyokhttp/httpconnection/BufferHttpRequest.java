package com.gym.easyokhttp.httpconnection;

import com.gym.easyokhttp.httpconnection.http.HttpHeader;
import com.gym.easyokhttp.httpconnection.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @funtion 请求流抽象处理BufferHttpRequest
 * @author lemon Guo
 */

public abstract class BufferHttpRequest extends AbstractHttpRequest {

    private ByteArrayOutputStream mByteArray = new ByteArrayOutputStream();

    protected OutputStream getBodyOutputStream() {
        return mByteArray;
    }

    protected HttpResponse executeInternal(HttpHeader header) throws IOException {
        byte[] data = mByteArray.toByteArray();
        return executeInternal(header, data);
    }

    protected abstract HttpResponse executeInternal(HttpHeader header, byte[] data) throws IOException;
}
