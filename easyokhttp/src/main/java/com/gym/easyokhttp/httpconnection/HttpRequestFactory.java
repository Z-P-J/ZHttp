package com.gym.easyokhttp.httpconnection;


import com.gym.easyokhttp.httpconnection.http.HttpMethod;
import com.gym.easyokhttp.httpconnection.http.HttpRequest;

import java.io.IOException;
import java.net.URI;

/**
 * @function 接口HttpRequestFactory（获取HttpRequest对象）
 * @author lemon Guo
 */

public interface HttpRequestFactory {

    HttpRequest createHttpRequest(URI uri, HttpMethod method) throws IOException;
}
