package com.zpj.http.parser.gson;

import com.google.gson.Gson;
import com.zpj.http.core.IHttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * @author Z-P-J
 */
public class GsonParser implements IHttp.Parser {

    private static final Pattern jsonContentTypeRxp = Pattern.compile("(application|text)/\\w*\\+?json.*");

    @Override
    public boolean accept(IHttp.Response response, Type type) {
        return jsonContentTypeRxp.matcher(response.contentType()).matches();
    }

    @Override
    public Object parse(IHttp.Response response, Type type) throws IOException {
        try (InputStream is = response.bodyStream();
             InputStreamReader inputStreamReader = new InputStreamReader(is, Charset.forName(response.charset()));
             BufferedReader reader = new BufferedReader(inputStreamReader)
        ) {
            return new Gson().fromJson(reader, type);
        } finally {
            response.close();
        }
    }

}
