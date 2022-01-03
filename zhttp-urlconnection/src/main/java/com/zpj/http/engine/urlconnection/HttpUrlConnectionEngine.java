package com.zpj.http.engine.urlconnection;

import com.zpj.http.core.HttpConfig;
import com.zpj.http.core.IHttp;
import com.zpj.http.engine.urlconnection.ssl.HttpsTrustManager;

/**
 * @author Z-P-J
 */
public class HttpUrlConnectionEngine implements IHttp.HttpEngine {
    @Override
    public IHttp.Request createRequest(HttpConfig config) {
        return new HttpRequestImpl(config);
    }

    @Override
    public IHttp.Connection createConnection(IHttp.Request request) {
        return new HttpConnectionImpl(request);
    }

    @Override
    public IHttp.Response createResponse(IHttp.Connection connection) {
        return new HttpResponseImpl(connection);
    }

    @Override
    public void initSSL(boolean isAllowAllSSL) {
        HttpsTrustManager.install(isAllowAllSSL);
    }

}
