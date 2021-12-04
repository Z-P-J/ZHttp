package com.zpj.http.core;

import android.util.Log;

import com.zpj.http.exception.HttpStatusException;
import com.zpj.http.exception.UncheckedIOException;
import com.zpj.http.io.ConstrainableInputStream;
import com.zpj.http.parser.html.utils.DataUtil;
import com.zpj.http.utils.StringUtil;
import com.zpj.http.utils.UrlUtil;
import com.zpj.http.utils.Validate;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public abstract class HttpResponse implements IHttp.Response {

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final int HTTP_TEMP_REDIR = 307; // http/1.1 temporary redirect, not in Java's set.
    public static final String DefaultUploadType = "application/octet-stream";

    protected final IHttp.Request req;
    protected final HttpConfig config;

    private ByteBuffer byteData;
    private InputStream bodyStream;

    private int numRedirects = 0;
    protected boolean executed = false;
    private boolean inputStreamRead = false;

    protected String charset;

    private ResponseInfo info;


    protected HttpResponse(IHttp.Request req) {
        this.req = req;
        this.config = req.config();
    }

    @Override
    public HttpConfig config() {
        return config;
    }

    @Override
    public final IHttp.Response execute() throws Exception {
        Validate.notNull(req, "Request must not be null");
        Validate.notNull(config.url(), "URL must be specified to connect");
        String protocol = config.url().getProtocol();
        if (!protocol.equals("http") && !protocol.equals("https"))
            throw new MalformedURLException("Only http & https protocols supported");
        final boolean methodHasBody = config.method().hasBody();
//        final boolean hasRequestBody = req.requestBody() != null;
//        final boolean hasRequestBody = false;
//        if (!methodHasBody)
//            Validate.isFalse(hasRequestBody, "Cannot set a request body for HTTP method " + req.method());
        if (!methodHasBody) {
            config.requestBody(null);
        }

        // set up the request for execution
//        String mimeBoundary = null;
//        if (config.data().size() > 0 && (!methodHasBody))
//            UrlUtil.serialiseRequestUrl(config);
//        else if (methodHasBody)
//            mimeBoundary = setOutputContentType();
        if (config.data().size() > 0 && (!methodHasBody))
            UrlUtil.serialiseRequestUrl(config);

        long startTime = System.nanoTime();

        try {
            info = onExecute(config);
            Log.d("HttpResponse", "execute info=" + info);

            if (info.headers.containsKey(HttpHeader.SET_COOKIE)) {
                config.cookie(info.headers.get(HttpHeader.SET_COOKIE));
            }
            if (config.cookieJar != null && config.cookies != null) {
                config.cookieJar.saveCookies(config.url, config.cookies);
            }

            // redirect if there's a location header (from 3xx, or 201 etc)
//                && req.followRedirects()
            if (hasHeader(HttpHeader.LOCATION)) {
                String location = header(HttpHeader.LOCATION);
                if (location.startsWith("http:/") && location.charAt(6) != '/') // fix broken Location: http:/temp/AAG_New/en/index.php
                    location = location.substring(6);
//                numRedirects++;
                if (++numRedirects > config.maxRedirectCount)
                    throw new IOException(String.format("Too many redirects occurred trying to load URL %s", config.url));
                if (config.getOnRedirectListener() == null || config.getOnRedirectListener().onRedirect(numRedirects, location)) {
                    if (statusCode() != HTTP_TEMP_REDIR) {
                        config.method(IHttp.Method.GET); // always redirect with a get. any data param from original req are dropped.
                        config.data().clear();
                        config.requestBody(null);
                        config.removeHeader(HttpHeader.CONTENT_TYPE);
                    }

                    URL redir = StringUtil.resolve(config.url(), location);
                    config.url(UrlUtil.encodeUrl(redir));

                    close();

                    return execute();
                }

            }
            if ((statusCode() < 200 || statusCode() >= 400) && !config.ignoreHttpErrors())
                throw new HttpStatusException("HTTP error fetching URL", statusCode(), config.url().toString());



            charset = DataUtil.getCharsetFromContentType(contentType()); // may be null, readInputStream deals with it
            if (contentLength() != 0 && config.method != IHttp.Method.HEAD) { // -1 means unknown, chunked. sun throws an IO exception on 500 response with no content when trying to read body
                bodyStream = null;
                bodyStream = info.getBodyStream();
                if (hasHeaderWithValue(HttpHeader.CONTENT_ENCODING, "gzip")) {
                    bodyStream = new GZIPInputStream(bodyStream);
                } else if (hasHeaderWithValue(HttpHeader.CONTENT_ENCODING, "deflate")) {
                    bodyStream = new InflaterInputStream(bodyStream, new Inflater(true));
                }
                bodyStream = ConstrainableInputStream
                        .wrap(bodyStream, DataUtil.bufferSize, config.maxBodySize)
                        .timeout(startTime, config.connectTimeout + config.readTimeout);
            } else {
                byteData = DataUtil.emptyByteBuffer();
            }
        } catch (IOException e) {
            // per Java's documentation, this is not necessary, and precludes keepalives. However in practice,
            // connection errors will not be released quickly enough and can cause a too many open files error.
            e.printStackTrace();

            close();
            throw e;
        }

        executed = true;
        return this;
    }

    @Override
    public String body() {
        prepareByteData();
        // charset gets set from header on execute, and from meta-equiv on parse. parse may not have happened yet
        String body;
        if (charset == null)
            body = Charset.forName(DataUtil.defaultCharset).decode(byteData).toString();
        else
            body = Charset.forName(charset).decode(byteData).toString();
        ((Buffer) byteData).rewind(); // cast to avoid covariant return type change in jdk9
        return body;
    }

    @Override
    public byte[] bodyAsBytes() {
        prepareByteData();
        return byteData.array();
    }

    @Override
    public IHttp.Response bufferUp() {
        prepareByteData();
        return this;
    }

    @Override
    public BufferedInputStream bodyStream() {
        Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before getting response body");
        Validate.isFalse(inputStreamRead, "Request has already been read");
        inputStreamRead = true;
        return ConstrainableInputStream.wrap(bodyStream, DataUtil.bufferSize, config.maxBodySize);
    }

    protected abstract ResponseInfo onExecute(HttpConfig config) throws Exception;

    @Override
    public void close() {
        closeIO();
        disconnect();
    }

    @Override
    public void closeIO() {
        if (bodyStream != null) {
            try {
                bodyStream.close();
            } catch (IOException e) {
                // no-op
            } finally {
                bodyStream = null;
            }
        }
    }

    @Override
    public boolean hasHeader(String name) {
        return headers().containsKey(name);
    }

    @Override
    public boolean hasHeaderWithValue(String name, String value) {
        return value.equalsIgnoreCase(header(name));
    }

    @Override
    public String header(String name) {
        return headers().get(name);
    }

    @Override
    public Map<String, String> headers() {
        return info.headers;
    }

    @Override
    public Map<String, String> cookies() {
        return config.cookies;
    }

    @Override
    public String cookieStr() {
        return config.cookieStr();
    }

    @Override
    public String cookie(String key) {
        return config.getCookie(key);
    }

    @Override
    public IHttp.Method method() {
        return this.config.method();
    }

    @Override
    public int statusCode() {
        return info.getStatusCode();
    }

    @Override
    public String statusMessage() {
        return info.getStatusMessage();
    }

    @Override
    public String charset() {
        return charset;
    }

    @Override
    public String contentType() {
        return info.getContentType();
    }

    @Override
    public long contentLength() {
        return info.getContentLength();
    }


















    protected String setOutputContentType() {
        String bound = null;
        if (config.hasHeader(HttpHeader.CONTENT_TYPE)) {
            // no-op; don't add content type as already set (e.g. for requestBody())
            // todo - if content type already set, we could add charset

            // if user has set content type to multipart/form-data, auto add boundary.
            if (config.header(HttpHeader.CONTENT_TYPE).contains(MULTIPART_FORM_DATA) &&
                    !config.header(HttpHeader.CONTENT_TYPE).contains("boundary")) {
                bound = DataUtil.mimeBoundary();
                config.header(HttpHeader.CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
            }

        } else if (config.needsMultipart()) {
            bound = DataUtil.mimeBoundary();
            config.header(HttpHeader.CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
        } else {
            config.header(HttpHeader.CONTENT_TYPE, FORM_URL_ENCODED + "; charset=" + config.postDataCharset());
        }
        return bound;
    }
    private void prepareByteData() {
        Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before getting response body");
        if (byteData == null) {
            Validate.isFalse(inputStreamRead, "Request has already been read (with .parse())");
            try {
                byteData = DataUtil.readToByteBuffer(bodyStream, config.maxBodySize);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                inputStreamRead = true;
                close();
            }
        }
    }


}
