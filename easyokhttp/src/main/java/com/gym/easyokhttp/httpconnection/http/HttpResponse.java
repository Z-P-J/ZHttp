package com.gym.easyokhttp.httpconnection.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @funtion Http响应接口
 * @author nate
 */
public interface HttpResponse extends Header, Closeable {

    HttpStatus getStatus();

    String getStatusMsg();

    InputStream getBody() throws IOException;

    void close();

    long getContentLength();

}
