package com.zpj.http.parser.ajsoup.decoder;


import com.zpj.http.parser.ajsoup.AJsoupReaderContext;
import com.zpj.http.parser.ajsoup.data.TypeLiteral;
import com.zpj.http.parser.ajsoup.kit.AnalysisDecoder;
import com.zpj.http.parser.jsoup.nodes.Element;
import com.zpj.http.parser.jsoup.select.Elements;

import java.lang.reflect.Constructor;

class ReflectionArrayDecoder implements Decoder {

    private final Class componentType;
    private final Decoder compTypeDecoder;
    private  Constructor ctor;

    public ReflectionArrayDecoder(Class clazz) {
        componentType = clazz.getComponentType();
        try {
            ctor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        compTypeDecoder = AnalysisDecoder.getDecoder(TypeLiteral.create(componentType).getDecoderCacheKey(), componentType);
    }

    @Override
    public Object decode(AJsoupReaderContext context)  {
        Object ctor[] = new Constructor[context.elements.size()];
        for (int i = 0; i < context.elements.size(); i++) {
            Element element = context.elements.get(i);
            ctor[i]=compTypeDecoder.decode( new AJsoupReaderContext(new Elements(element),context.resource));
        }
        return ctor;
    }


}
