package com.gym.easyokhttp.httpconnection.service;

import com.gym.easyokhttp.httpconnection.service.convert.Convert;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @funtion WrapperResponse类型转换封装 Response
 * @author lemon Guo
 */

public class WrapperResponse extends MultiThreadResponse<String> {

    private MultiThreadResponse mMoocResponse;

    private List<Convert> mConvert;

    public WrapperResponse(MultiThreadResponse moocResponse, List<Convert> converts) {
        this.mMoocResponse = moocResponse;
        this.mConvert = converts;
    }

    @Override
    public void success(MultiThreadRequest request, String data) {

        for (Convert convert : mConvert) {
            if (convert.isCanParse(request.getContentType())) {
                try {
                    Object object = convert.parse(data, getType());
                    mMoocResponse.success(request, object);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    public Type getType() {
        Type type = mMoocResponse.getClass().getGenericSuperclass();
        Type[] paramType = ((ParameterizedType) type).getActualTypeArguments();
        return paramType[0];
    }

    @Override
    public void fail(int errorCode, String errorMsg) {
    }
}
