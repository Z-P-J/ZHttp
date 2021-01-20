package com.zpj.http.core;

import android.text.TextUtils;

import com.zpj.http.ZHttp;
import com.zpj.http.parser.html.nodes.Document;
import com.zpj.http.utils.UrlUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class HttpConfig extends BaseConfig<HttpConfig> {

    private final Collection<IHttp.KeyVal> data = new ArrayList<>();

    protected URL originalUrl;
    protected URL url;
    protected IHttp.Method method = IHttp.Method.GET;
    protected String body = null;

    protected HttpConfig() {
        ZHttp.HttpGlobalConfig globalConfig = ZHttp.config();
        this.proxy(globalConfig.proxy())
                .baseUrl(globalConfig.baseUrl())
                .debug(globalConfig.debug())
                .cookies(globalConfig.cookies())
                .userAgent(globalConfig.userAgent())
                .connectTimeout(globalConfig.connectTimeout())
                .readTimeout(globalConfig.readTimeout())
                .retryCount(globalConfig.retryCount())
                .retryDelay(globalConfig.retryDelay())
                .bufferSize(globalConfig.bufferSize())
                .maxBodySize(globalConfig.maxBodySize())
                .allowAllSSL(globalConfig.allowAllSSL())
                .headers(globalConfig.headers())
                .ignoreContentType(globalConfig.ignoreContentType())
                .ignoreHttpErrors(globalConfig.ignoreHttpErrors())
                .sslSocketFactory(globalConfig.sslSocketFactory())
                .maxRedirectCount(globalConfig.maxRedirectCount())
                .onRedirect(globalConfig.getOnRedirectListener());
    }

    public URL url() {
        return url;
    }

    public URL getOriginalUrl() {
        return originalUrl;
    }

    public HttpConfig url(String url) {
        try {
            url = url.trim();
            String tempUrl = url.toLowerCase();
            boolean isHttp = tempUrl.startsWith("http://") || tempUrl.startsWith("https://");
            if (!isHttp) {
                if (tempUrl.contains("://")) {
                    throw new MalformedURLException("Only http and https protocols supported!");
                }
                if (baseUrl == null) {
                    throw new MalformedURLException("You must set baseUrl firstly!");
                }
                if (!tempUrl.startsWith("/")) {
                    url = "/" + url;
                }
                url = baseUrl.resolve(url).toString();
            }
            this.url = new URL(UrlUtil.encodeUrl(url));
            if (this.originalUrl == null) {
                this.originalUrl = this.url;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed URL: " + url, e);
        }
        return this;
    }

    public HttpConfig url(URL url) {
//        this.url = url;
//        if (this.originalUrl == null) {
//            this.originalUrl = this.url;
//        }
//        return this;
        return url(url.toString());
    }

    public IHttp.Method method() {
        return method;
    }

    public HttpConfig method(IHttp.Method method) {
        this.method = method;
        return this;
    }

    public HttpConfig range(String range) {
        this.headers.put(HttpHeader.RANGE, range);
        return this;
    }

    public HttpConfig range(long start) {
        this.headers.put(HttpHeader.RANGE, String.format(Locale.ENGLISH, "bytes=%d-", start));
        return this;
    }

    public HttpConfig range(long start, long end) {
        this.headers.put(HttpHeader.RANGE, String.format(Locale.ENGLISH, "bytes=%d-%d", start, end));
        return this;
    }

    public HttpConfig requestBody(String body) {
        this.body = body;
        return this;
    }

    public String requestBody() {
        return body;
    }

    public HttpConfig data(IHttp.KeyVal keyval) {
        if (keyval != null) {
            data.add(keyval);
        }
        return this;
    }

    public Collection<IHttp.KeyVal> data() {
        return data;
    }

    public HttpConfig data(String key, String value) {
        return data(HttpKeyVal.create(key, value));
    }

    public HttpConfig data(String key, String filename, InputStream inputStream) {
        return data(HttpKeyVal.create(key, filename, inputStream));
    }

    public HttpConfig data(String key, String filename, InputStream inputStream, IHttp.OnStreamWriteListener listener) {
        return data(HttpKeyVal.create(key, filename, inputStream, listener));
    }

    public HttpConfig data(String key, String filename, InputStream inputStream, String contentType) {
        return data(HttpKeyVal.create(key, filename, inputStream).contentType(contentType));
    }

    public HttpConfig data(Map<String, String> data) {
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                data(HttpKeyVal.create(entry.getKey(), entry.getValue()));
            }
        }
        return this;
    }

//    public HttpRequest3 data(String... keyvals) {
//        Validate.notNull(keyvals, "Data key value pairs must not be null");
//        Validate.isTrue(keyvals.length % 2 == 0, "Must supply an even number of key value pairs");
//        for (int i = 0; i < keyvals.length; i += 2) {
//            String key = keyvals[i];
//            String value = keyvals[i + 1];
//            Validate.notEmpty(key, "Data key must not be empty");
//            Validate.notNull(value, "Data value must not be null");
//            data(HttpKeyVal.create(key, value));
//        }
//        return this;
//    }

    public HttpConfig data(Collection<IHttp.KeyVal> data) {
        if (data != null) {
            for (IHttp.KeyVal entry : data) {
                data(entry);
            }
        }
        return this;
    }

    public IHttp.KeyVal data(String key) {
        if (!TextUtils.isEmpty(key)) {
            for (IHttp.KeyVal keyVal : data()) {
                if (keyVal.key().equals(key))
                    return keyVal;
            }
        }
        return null;
    }

    public boolean needsMultipart() {
        // multipart mode, for files. add the header if we see something with an inputstream, and return a non-null boundary
        for (IHttp.KeyVal keyVal : data()) {
            if (keyVal.hasInputStream())
                return true;
        }
        return false;
    }

    public IHttp.Request request() {
        return new HttpRequestImpl(this);
    }

    public IHttp.Response syncExecute() throws Exception {
        return request().syncExecute();
    }

    public String syncToStr() throws Exception {
        return request().syncToStr();
    }

    public Document syncToHtml() throws Exception {
        return request().syncToHtml();
    }

    public JSONObject syncToJsonObject() throws Exception {
        return request().syncToJsonObject();
    }

    public JSONArray syncToJsonArray() throws Exception {
        return request().syncToJsonArray();
    }

    public Document syncToXml() throws Exception {
        return request().syncToXml();
    }


    public final HttpObserver<IHttp.Response> execute() {
        return request().execute();
    }

    public final HttpObserver<String> toStr() {
        return request().toStr();
    }

    public final HttpObserver<Document> toHtml() {
        return request().toHtml();
    }

    public final HttpObserver<JSONObject> toJsonObject() {
        return request().toJsonObject();
    }

    public final HttpObserver<JSONArray> toJsonArray() {
        return request().toJsonArray();
    }

    public final HttpObserver<Document> toXml() {
        return request().toXml();
    }

}
