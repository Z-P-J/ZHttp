package com.zpj.http.parser;

import android.util.Log;

import com.zpj.http.core.IHttp;
import com.zpj.http.parser.html.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 解析为字符串
 * @author Z-P-J
 */
public class JsoupParser implements IHttp.Parser<Document> {

    private static final String TAG = "StringParser";

    @Override
    public Document parse(IHttp.Response response) throws IOException {
        try (InputStream is = response.bodyStream()) {
            return Jsoup.parse(new StringParser().parse(response));
        } finally {
            response.close();
        }

    }

}
