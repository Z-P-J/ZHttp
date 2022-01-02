package com.zpj.http.impl;

import com.zpj.http.core.IHttp;
import com.zpj.http.ssl.HttpsTrustManager;

/**
 * @author Z-P-J
 */
public class HttpUrlEngine implements IHttp.HttpEngine {

    private final IHttp.HttpFactory httpFactory;
    private final IHttp.HttpDispatcher httpDispatcher;
    private final IHttp.CookieJar cookieJar;

    public HttpUrlEngine() {
        this(new Builder());
    }

    private HttpUrlEngine(Builder builder) {
        this.httpFactory = builder.httpFactory;
        this.httpDispatcher = builder.httpDispatcher;
        this.cookieJar = builder.cookieJar;
    }

    @Override
    public IHttp.HttpFactory getHttpFactory() {
        return httpFactory;
    }

    @Override
    public IHttp.HttpDispatcher getHttpDispatcher() {
        return httpDispatcher;
    }

    @Override
    public IHttp.CookieJar getCookieJar() {
        return cookieJar;
    }

    @Override
    public void initSSL(boolean isAllowAllSSL) {
        HttpsTrustManager.install(isAllowAllSSL);
    }

    public static class Builder {

        private IHttp.HttpFactory httpFactory = new HttpFactoryImpl();
        private IHttp.HttpDispatcher httpDispatcher = new HttpDispatcherImpl();
        private IHttp.CookieJar cookieJar = new HttpCookieJarImpl();

        public Builder setHttpDispatcher(IHttp.HttpDispatcher httpDispatcher) {
            this.httpDispatcher = httpDispatcher;
            return this;
        }

        public Builder setHttpFactory(IHttp.HttpFactory httpFactory) {
            this.httpFactory = httpFactory;
            return this;
        }

        public Builder setCookieJar(IHttp.CookieJar cookieJar) {
            this.cookieJar = cookieJar;
            return this;
        }

        public HttpUrlEngine build() {
            return new HttpUrlEngine(this);
        }

    }

}
