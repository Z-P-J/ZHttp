package com.zpj.http.impl;

import com.zpj.http.core.IHttp;
import com.zpj.http.utils.Validate;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Z-P-J
 */
public class ParserFactoryImpl implements IHttp.ParserFactory {

    private final Map<Type, IHttp.Parser<?>> parserMap = new HashMap<>();

    @Override
    public <T> void register(Type type, IHttp.Parser<T> parser) {
        synchronized (parserMap) {
            parserMap.put(type, parser);
        }
    }

    @Override
    public <T> IHttp.Parser<T> create(Type type) {
        IHttp.Parser<?> parser;
        synchronized (parserMap) {
            parser = parserMap.get(type);
        }
        Validate.notNull(parser, "Unsupported type to parse! type=" + type);
        return (IHttp.Parser<T>) parser;
    }

}
