package com.zpj.http.engine.urlconnection;

import com.zpj.http.core.HttpResponse;
import com.zpj.http.core.IHttp;

/**
 * @author Z-P-J
 */
public class HttpResponseImpl extends HttpResponse {

    public HttpResponseImpl(IHttp.Connection connection) {
        super(connection);
    }

}
