package com.zpj.http;

import com.zpj.http.core.BaseConfig;
import com.zpj.http.core.HttpConfig;
import com.zpj.http.core.IHttp;
import com.zpj.http.impl.ParserFactoryImpl;
import com.zpj.http.utils.Validate;

public final class ZHttp {

    private static final class Holder {
        private static final HttpGlobalConfig HTTP_CONFIG = new HttpGlobalConfig();
    }

    private ZHttp() {}

    public static HttpGlobalConfig config() {
        return Holder.HTTP_CONFIG;
    }

    private static HttpConfig newConfig() {
        return new HttpConfig()
                .baseUrl(config().baseUrl())
                .debug(config().debug())
                .headers(config().headers())
                .bufferSize(config().bufferSize())
                .maxBodySize(config().maxBodySize())
                .maxRedirectCount(config().maxRedirectCount())
                .ignoreHttpErrors(config().ignoreHttpErrors())
                .ignoreContentType(config().ignoreContentType())
                .postDataCharset(config().postDataCharset())
                .retryCount(config().retryCount())
                .cookies(config().cookies())
                .retryDelay(config().retryDelay())
                .connectTimeout(config().connectTimeout())
                .readTimeout(config().readTimeout())
                .allowAllSSL(config().allowAllSSL())
                .sslSocketFactory(config().sslSocketFactory())
                .proxy(config().proxy())
                .maxRedirectCount(config().maxRedirectCount())
                .onRedirect(config().getOnRedirectListener())
                .cookieJar(config().httpEngine().getCookieJar())
                .httpFactory(config().httpEngine().getHttpFactory())
                .httpDispatcher(config().httpEngine().getHttpDispatcher());
    }

    public static HttpConfig connect(String url) {
        return newConfig().url(url);
    }

    public static HttpConfig get(String url) {
        return connect(url).method(IHttp.Method.GET);
    }

    public static HttpConfig post(String url) {
        return connect(url).method(IHttp.Method.POST);
    }

    public static HttpConfig head(String url) {
        return connect(url).method(IHttp.Method.HEAD);
    }

    public static HttpConfig put(String url) {
        return connect(url).method(IHttp.Method.PUT);
    }

    public static HttpConfig delete(String url) {
        return connect(url).method(IHttp.Method.DELETE);
    }

    public static HttpConfig patch(String url) {
        return connect(url).method(IHttp.Method.PATCH);
    }

    public static HttpConfig options(String url) {
        return connect(url).method(IHttp.Method.OPTIONS);
    }

    public static HttpConfig trace(String url) {
        return connect(url).method(IHttp.Method.TRACE);
    }


    public static class HttpGlobalConfig extends BaseConfig<HttpGlobalConfig> {

        private IHttp.HttpEngine httpEngine;
        private IHttp.ParserFactory parserFactory = new ParserFactoryImpl();

        private HttpGlobalConfig() {

        }

        public IHttp.HttpEngine httpEngine() {
            Validate.notNull(httpEngine, "HttpEngine must not be null");
            return httpEngine;
        }

        public HttpGlobalConfig httpEngine(IHttp.HttpEngine httpEngine) {
            this.httpEngine = httpEngine;
            return this;
        }

        public HttpGlobalConfig parserFactory(IHttp.ParserFactory parserFactory) {
            this.parserFactory = parserFactory;
            return this;
        }

        public IHttp.ParserFactory parserFactory() {
            return parserFactory;
        }

        public void init() {
            httpEngine().initSSL(allowAllSSL());
        }

    }


}
