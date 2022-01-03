package com.zpj.http.core;

import com.zpj.http.core.IHttp;
import com.zpj.http.utils.Validate;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Z-P-J
 * TODO 完善Parser
 */
public class HttpParserFactory implements IHttp.ParserFactory {

    private final Set<IHttp.Parser> parserSet = new HashSet<>();

    @Override
    public void register(IHttp.Parser parser) {
        // TODO
        synchronized (parserSet) {
            parserSet.add(parser);
        }
    }

    @Override
    public IHttp.Parser create(Type type) {
//        IHttp.Parser parser;
//        synchronized (parserSet) {
//            parser = parserSet.get(type);
//        }
//        Validate.notNull(parser, "Unsupported type to parse! type=" + type);
//        return parser;
        // TODO
        return null;
    }

}
