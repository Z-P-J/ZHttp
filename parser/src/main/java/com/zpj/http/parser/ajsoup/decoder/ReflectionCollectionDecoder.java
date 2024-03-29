package com.zpj.http.parser.ajsoup.decoder;


import com.zpj.http.parser.ajsoup.AJsoupReaderContext;
import com.zpj.http.parser.ajsoup.data.TypeLiteral;
import com.zpj.http.parser.ajsoup.exception.AJsoupReaderException;
import com.zpj.http.parser.ajsoup.kit.AnalysisDecoder;
import com.zpj.http.parser.jsoup.nodes.Element;
import com.zpj.http.parser.jsoup.select.Elements;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Collection;

class ReflectionCollectionDecoder implements Decoder {
    private final Constructor ctor;
    private final Decoder compTypeDecoder;

    public ReflectionCollectionDecoder(Class clazz, Type[] typeArgs) {
        try {
            ctor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new AJsoupReaderException(e);
        }
        // List<T>  T 的 解析器
        compTypeDecoder = AnalysisDecoder.getDecoder(TypeLiteral.create(typeArgs[0]).getDecoderCacheKey(), typeArgs[0]);
    }

    @Override
    public Object decode(AJsoupReaderContext context)  {
            return decode_(context);
    }

    private Object decode_(AJsoupReaderContext context)  {
        Collection  col = null;
        try {
            col = (Collection) this.ctor.newInstance();
            for (Element element : context.elements) {   //  bug  当 List<T>  t 为基本类型
                Object decode = compTypeDecoder.decode( new AJsoupReaderContext(new Elements(element),context.resource.annotations));
                col.add(decode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AJsoupReaderException("list decode failure");
        }

        return col;
    }
}
