package com.gym.easyokhttp.httpconnection.service;

import com.gym.easyokhttp.httpconnection.http.HttpMethod;
import com.gym.easyokhttp.httpconnection.service.convert.Convert;
import com.gym.easyokhttp.httpconnection.service.convert.JsonConvert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/2.
 */

public class HttpApiProvider {

    private static final String ENCODING = "utf-8";

    private static WorkStation sWorkStation = new WorkStation();

    private static final List<Convert> sConverts = new ArrayList<>();

    static {
        sConverts.add(new JsonConvert());
    }


    /*
    *   对请求参数进行编码处理
    * */
    public static byte[] encodeParam(Map<String, String> value) {
        if (value == null || value.size() == 0) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        int count = 0;
        try {
            for (Map.Entry<String, String> entry : value.entrySet()) {

                buffer.append(URLEncoder.encode(entry.getKey(), ENCODING)).append("=").
                        append(URLEncoder.encode(entry.getValue(), ENCODING));
                if (count != value.size() - 1) {
                    buffer.append("&");
                }
                count++;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return buffer.toString().getBytes();
    }

    public static void helloWorld(String ul, Map<String, String> value, MultiThreadResponse response) {
        MultiThreadRequest request = new MultiThreadRequest();
        WrapperResponse wrapperResponse = new WrapperResponse(response, sConverts);
        request.setUrl(ul);
        request.setMethod(HttpMethod.POST);
        request.setData(encodeParam(value));
//        request.setResponse(response);
        request.setResponse(wrapperResponse);
        sWorkStation.add(request);
    }

}
