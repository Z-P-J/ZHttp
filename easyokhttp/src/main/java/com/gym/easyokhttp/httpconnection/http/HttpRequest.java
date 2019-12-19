package com.gym.easyokhttp.httpconnection.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * @funtion HttpRequest接口设计
 * @author nate
 */
public interface HttpRequest extends Header {

    HttpMethod getMethod();

    URI getUri();

    OutputStream getBody();

    HttpResponse execute() throws IOException;
}
