package com.zpj.http.parser.ajsoup.decoder;


import com.zpj.http.parser.ajsoup.data.ClassDescriptor;
import com.zpj.http.parser.ajsoup.kit.AnalysisDecoder;

/**
 * Created by zoudong on 2017/3/12.
 */

public abstract class BaseDecoder implements Decoder {
    protected final ClassDescriptor desc;
    public BaseDecoder(Class clazz) {
        desc = AnalysisDecoder.createDecoder(clazz,null);
    }
}
