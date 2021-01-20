package com.zpj.http.core;

import com.zpj.http.parser.html.Parser;
import com.zpj.http.parser.html.nodes.Document;
import com.zpj.http.utils.Validate;

import org.json.JSONArray;
import org.json.JSONObject;

public class HttpRequestImpl extends HttpRequest {

    HttpRequestImpl(HttpConfig config) {
        super(config);
    }

    @Override
    public IHttp.Response syncExecute() throws Exception {
        return response().execute();
    }

    @Override
    public String syncToStr() throws Exception {
        return syncExecute().body();
    }

    @Override
    public Document syncToHtml() throws Exception {
        IHttp.Response response = syncExecute();
        if (!config.ignoreContentType) {
            Validate.isHtml(response.contentType(), config.url.toString());
        }
        return Parser.htmlParser().parseInput(response.body(), config.url.toExternalForm());
    }

    @Override
    public JSONObject syncToJsonObject() throws Exception {
        IHttp.Response response = syncExecute();
        if (!config.ignoreContentType) {
            Validate.isJson(response.contentType(), config.url.toString());
        }
        return new JSONObject(response.body());
    }

    @Override
    public JSONArray syncToJsonArray() throws Exception {
        IHttp.Response response = syncExecute();
        if (!config.ignoreContentType) {
            Validate.isJson(response.contentType(), config.url.toString());
        }
        return new JSONArray(response.body());
    }

    @Override
    public Document syncToXml() throws Exception {
        IHttp.Response response = syncExecute();
        if (!config.ignoreContentType) {
            Validate.isXml(response.contentType(), config.url.toString());
        }
        return Parser.xmlParser().parseInput(response.body(), config.url.toExternalForm());
    }

}
