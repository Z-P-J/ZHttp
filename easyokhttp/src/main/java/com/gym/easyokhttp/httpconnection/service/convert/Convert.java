package com.gym.easyokhttp.httpconnection.service.convert;

import com.gym.easyokhttp.httpconnection.http.HttpResponse;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @funtion 数据类型转换接口
 * @author lemon Guo
 */

public interface Convert {

    Object parse(HttpResponse response, Type type) throws IOException;

    boolean isCanParse(String contentType);

    Object parse(String content, Type type) throws IOException;
}
