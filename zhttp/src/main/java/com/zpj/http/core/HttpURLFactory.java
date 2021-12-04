package com.zpj.http.core;

public class HttpURLFactory implements IHttp.HttpFactory {

    @Override
    public IHttp.Request createRequest(HttpConfig config) {
        return new HttpRequestImpl(config);
    }

    @Override
    public IHttp.Response createResponse(IHttp.Request request) {
        return new HttpResponseImpl(request);
    }

//    @Override
//    public IHttp.HttpEngine createHttpEngine() {
//        return new HttpURLEngine();
//    }

}
