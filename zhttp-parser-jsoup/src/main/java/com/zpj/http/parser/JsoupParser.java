package com.zpj.http.parser;

import android.util.Log;

import com.zpj.http.core.IHttp;
import com.zpj.http.parser.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * 解析为字符串
 *
 * @author Z-P-J
 */
public class JsoupParser implements IHttp.Parser {

    private static final String TAG = "StringParser";

    @Override
    public boolean accept(IHttp.Response response, Type type) {
        // TODO
        return true;
    }

    @Override
    public Document parse(IHttp.Response response, Type type) throws IOException {
        try (InputStream is = response.bodyStream();
             InputStreamReader inputStreamReader = new InputStreamReader(is, Charset.forName(response.charset()));
             BufferedReader reader = new BufferedReader(inputStreamReader)
        ) {
            return Jsoup.parse(reader);
        } finally {
            response.close();
        }
    }
}
