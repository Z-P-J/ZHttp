package com.zpj.http.core;

import com.zpj.http.exception.HttpStatusException;
import com.zpj.http.exception.UncheckedIOException;
import com.zpj.http.exception.UnsupportedMimeTypeException;
import com.zpj.http.io.ConstrainableInputStream;
import com.zpj.http.parser.html.Parser;
import com.zpj.http.parser.html.TokenQueue;
import com.zpj.http.parser.html.nodes.Document;
import com.zpj.http.utils.DataUtil;
import com.zpj.http.utils.StringUtil;
import com.zpj.http.utils.UrlUtil;
import com.zpj.http.utils.Validate;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;

import static com.zpj.http.core.Connection.Method.HEAD;

public class HttpResponse extends HttpBase<Connection.Response> implements Connection.Response {

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    private static final int HTTP_TEMP_REDIR = 307; // http/1.1 temporary redirect, not in Java's set.
    private static final String DefaultUploadType = "application/octet-stream";
    private static final int MAX_REDIRECTS = 20;
    private int statusCode;
    private String statusMessage;
    private ByteBuffer byteData;
    private InputStream bodyStream;
    private HttpURLConnection conn;
    private String charset;
    private String contentType;
    private boolean executed = false;
    private boolean inputStreamRead = false;
    private int numRedirects = 0;
    private Connection.Request req;

    /*
     * Matches XML content types (like text/xml, application/xhtml+xml;charset=UTF8, etc)
     */
    private static final Pattern xmlContentTypeRxp = Pattern.compile("(application|text)/\\w*\\+?xml.*");

    HttpResponse() {
        super();
    }

    private HttpResponse(HttpResponse previousResponse) throws IOException {
        super();
        if (previousResponse != null) {
            numRedirects = previousResponse.numRedirects + 1;
            if (numRedirects >= MAX_REDIRECTS)
                throw new IOException(String.format("Too many redirects occurred trying to load URL %s", previousResponse.url()));
        }
    }

    static HttpResponse execute(Connection.Request req) throws IOException {
        return execute(req, null);
    }

    static HttpResponse execute(Connection.Request req, HttpResponse previousResponse) throws IOException {
        Validate.notNull(req, "Request must not be null");
        Validate.notNull(req.url(), "URL must be specified to connect");
        String protocol = req.url().getProtocol();
        if (!protocol.equals("http") && !protocol.equals("https"))
            throw new MalformedURLException("Only http & https protocols supported");
        final boolean methodHasBody = req.method().hasBody();
        final boolean hasRequestBody = req.requestBody() != null;
        if (!methodHasBody)
            Validate.isFalse(hasRequestBody, "Cannot set a request body for HTTP method " + req.method());

        // set up the request for execution
        String mimeBoundary = null;
        if (req.data().size() > 0 && (!methodHasBody || hasRequestBody))
            serialiseRequestUrl(req);
        else if (methodHasBody)
            mimeBoundary = setOutputContentType(req);

        long startTime = System.nanoTime();
        HttpURLConnection conn = createConnection(req);
        HttpResponse res;
        try {
            conn.connect();
            if (conn.getDoOutput())
                writePost(req, conn.getOutputStream(), mimeBoundary);

            int status = conn.getResponseCode();
            res = new HttpResponse(previousResponse);
            res.setupFromConnection(conn, previousResponse);
            res.req = req;

            // redirect if there's a location header (from 3xx, or 201 etc)
//                && req.followRedirects()
            if (res.hasHeader(HttpHeader.LOCATION)) {
                String location = res.header(HttpHeader.LOCATION);
                if (location.startsWith("http:/") && location.charAt(6) != '/') // fix broken Location: http:/temp/AAG_New/en/index.php
                    location = location.substring(6);
                if (req.getOnRedirectListener() == null || req.getOnRedirectListener().onRedirect(location)) {
                    if (status != HTTP_TEMP_REDIR) {
                        req.method(Connection.Method.GET); // always redirect with a get. any data param from original req are dropped.
                        req.data().clear();
                        req.requestBody(null);
                        req.removeHeader(HttpHeader.CONTENT_TYPE);
                    }

                    URL redir = StringUtil.resolve(req.url(), location);
                    req.url(UrlUtil.encodeUrl(redir));

                    for (Map.Entry<String, String> cookie : res.cookies.entrySet()) { // add response cookies to request (for e.g. login posts)
                        req.cookie(cookie.getKey(), cookie.getValue());
                    }
                    return execute(req, res);
                }

            }
            if ((status < 200 || status >= 400) && !req.ignoreHttpErrors())
                throw new HttpStatusException("HTTP error fetching URL", status, req.url().toString());

            // check that we can handle the returned content type; if not, abort before fetching it
            String contentType = res.contentType();
            if (contentType != null
                    && !req.ignoreContentType()
                    && !contentType.startsWith("text/")
                    && !contentType.contains("application/json")
                    && !xmlContentTypeRxp.matcher(contentType).matches()
            )
                throw new UnsupportedMimeTypeException("Unhandled content type. Must be text/*, application/xml, application/xhtml+xml or application/json",
                        contentType, req.url().toString());

            // switch to the XML parser if content type is xml and not parser not explicitly set
            if (contentType != null && xmlContentTypeRxp.matcher(contentType).matches()) {
                // only flip it if a HttpConnection.Request (i.e. don't presume other impls want it):
                if (req instanceof HttpRequest && !((HttpRequest)req).isParserDefined()) {
                    req.parser(Parser.xmlParser());
                }
            }

            res.charset = DataUtil.getCharsetFromContentType(res.contentType); // may be null, readInputStream deals with it
            if (conn.getContentLength() != 0 && req.method() != HEAD) { // -1 means unknown, chunked. sun throws an IO exception on 500 response with no content when trying to read body
                res.bodyStream = null;
                res.bodyStream = conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();
                if (res.hasHeaderWithValue(HttpHeader.CONTENT_ENCODING, "gzip")) {
                    res.bodyStream = new GZIPInputStream(res.bodyStream);
                } else if (res.hasHeaderWithValue(HttpHeader.CONTENT_ENCODING, "deflate")) {
                    res.bodyStream = new InflaterInputStream(res.bodyStream, new Inflater(true));
                }
                res.bodyStream = ConstrainableInputStream
                        .wrap(res.bodyStream, DataUtil.bufferSize, req.maxBodySize())
                        .timeout(startTime, req.timeout())
                ;
            } else {
                res.byteData = DataUtil.emptyByteBuffer();
            }
        } catch (IOException e){
            // per Java's documentation, this is not necessary, and precludes keepalives. However in practice,
            // connection errors will not be released quickly enough and can cause a too many open files error.
            conn.disconnect();
            throw e;
        }

        res.executed = true;
        return res;
    }

    public int statusCode() {
        return statusCode;
    }

    public String statusMessage() {
        return statusMessage;
    }

    public String charset() {
        return charset;
    }

    public HttpResponse charset(String charset) {
        this.charset = charset;
        return this;
    }

    public String contentType() {
        return contentType;
    }

    public Document parse() throws IOException {
        Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before parsing response");
        if (byteData != null) { // bytes have been read in to the buffer, parse that
            bodyStream = new ByteArrayInputStream(byteData.array());
            inputStreamRead = false; // ok to reparse if in bytes
        }
        Validate.isFalse(inputStreamRead, "Input stream already read and parsed, cannot re-read.");
        Document doc = DataUtil.parseInputStream(bodyStream, charset, url.toExternalForm(), req.parser());
        charset = doc.outputSettings().charset().name(); // update charset from meta-equiv, possibly
        inputStreamRead = true;
        safeClose();
        return doc;
    }

    private void prepareByteData() {
        Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before getting response body");
        if (byteData == null) {
            Validate.isFalse(inputStreamRead, "Request has already been read (with .parse())");
            try {
                byteData = DataUtil.readToByteBuffer(bodyStream, req.maxBodySize());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                inputStreamRead = true;
                safeClose();
            }
        }
    }

    public String body() {
        prepareByteData();
        // charset gets set from header on execute, and from meta-equiv on parse. parse may not have happened yet
        String body;
        if (charset == null)
            body = Charset.forName(DataUtil.defaultCharset).decode(byteData).toString();
        else
            body = Charset.forName(charset).decode(byteData).toString();
        ((Buffer)byteData).rewind(); // cast to avoid covariant return type change in jdk9
        return body;
    }

    public byte[] bodyAsBytes() {
        prepareByteData();
        return byteData.array();
    }

    @Override
    public Connection.Response bufferUp() {
        prepareByteData();
        return this;
    }

    @Override
    public BufferedInputStream bodyStream() {
        Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before getting response body");
        Validate.isFalse(inputStreamRead, "Request has already been read");
        inputStreamRead = true;
        return ConstrainableInputStream.wrap(bodyStream, DataUtil.bufferSize, req.maxBodySize());
    }

    // set up connection defaults, and details from request
    private static HttpURLConnection createConnection(Connection.Request req) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) (
                req.proxy() == null ?
                        req.url().openConnection() :
                        req.url().openConnection(req.proxy())
        );

        conn.setRequestMethod(req.method().name());
        conn.setInstanceFollowRedirects(false); // don't rely on native redirection support
        conn.setConnectTimeout(req.timeout());
        conn.setReadTimeout(req.timeout() / 2); // gets reduced after connection is made and status is read

        if (req.sslSocketFactory() != null && conn instanceof HttpsURLConnection)
            ((HttpsURLConnection) conn).setSSLSocketFactory(req.sslSocketFactory());
        if (req.method().hasBody())
            conn.setDoOutput(true);
        if (req.cookies().size() > 0)
            conn.addRequestProperty(HttpHeader.COOKIE, getRequestCookieString(req));
        for (Map.Entry<String, List<String>> header : req.multiHeaders().entrySet()) {
            for (String value : header.getValue()) {
                conn.addRequestProperty(header.getKey(), value);
            }
        }
        return conn;
    }

    /**
     * Call on completion of stream read, to close the body (or error) stream. The connection.disconnect allows
     * keep-alives to work (as the underlying connection is actually held open, despite the name).
     */
    private void safeClose() {
        if (bodyStream != null) {
            try {
                bodyStream.close();
            } catch (IOException e) {
                // no-op
            } finally {
                bodyStream = null;
            }
        }
        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
    }

    // set up url, method, header, cookies
    private void setupFromConnection(HttpURLConnection conn, HttpResponse previousResponse) throws IOException {
        this.conn = conn;
        method = Connection.Method.valueOf(conn.getRequestMethod());
        url = conn.getURL();
        statusCode = conn.getResponseCode();
        statusMessage = conn.getResponseMessage();
        contentType = conn.getContentType();

        Map<String, List<String>> resHeaders = createHeaderMap(conn);
        processResponseHeaders(resHeaders);

        // if from a redirect, map previous response cookies into this response
        if (previousResponse != null) {
            for (Map.Entry<String, String> prevCookie : previousResponse.cookies().entrySet()) {
                if (!hasCookie(prevCookie.getKey()))
                    cookie(prevCookie.getKey(), prevCookie.getValue());
            }
            previousResponse.safeClose();
        }
    }

    private static LinkedHashMap<String, List<String>> createHeaderMap(HttpURLConnection conn) {
        // the default sun impl of conn.getHeaderFields() returns header values out of order
        final LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
        int i = 0;
        while (true) {
            final String key = conn.getHeaderFieldKey(i);
            final String val = conn.getHeaderField(i);
            if (key == null && val == null)
                break;
            i++;
            if (key == null || val == null)
                continue; // skip http1.1 line

            if (headers.containsKey(key))
                headers.get(key).add(val);
            else {
                final ArrayList<String> vals = new ArrayList<>();
                vals.add(val);
                headers.put(key, vals);
            }
        }
        return headers;
    }

    void processResponseHeaders(Map<String, List<String>> resHeaders) {
        for (Map.Entry<String, List<String>> entry : resHeaders.entrySet()) {
            String name = entry.getKey();
            if (name == null)
                continue; // http/1.1 line

            List<String> values = entry.getValue();
            if (name.equalsIgnoreCase("Set-Cookie")) {
                for (String value : values) {
                    if (value == null)
                        continue;
                    TokenQueue cd = new TokenQueue(value);
                    String cookieName = cd.chompTo("=").trim();
                    String cookieVal = cd.consumeTo(";").trim();
                    // ignores path, date, domain, validateTLSCertificates et al. req'd?
                    // name not blank, value not null
                    if (cookieName.length() > 0)
                        cookie(cookieName, cookieVal);
                }
            }
            for (String value : values) {
                addHeader(name, value);
            }
        }
    }

    private static String setOutputContentType(final Connection.Request req) {
        String bound = null;
        if (req.hasHeader(HttpHeader.CONTENT_TYPE)) {
            // no-op; don't add content type as already set (e.g. for requestBody())
            // todo - if content type already set, we could add charset

            // if user has set content type to multipart/form-data, auto add boundary.
            if(req.header(HttpHeader.CONTENT_TYPE).contains(MULTIPART_FORM_DATA) &&
                    !req.header(HttpHeader.CONTENT_TYPE).contains("boundary")) {
                bound = DataUtil.mimeBoundary();
                req.header(HttpHeader.CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
            }

        }
        else if (needsMultipart(req)) {
            bound = DataUtil.mimeBoundary();
            req.header(HttpHeader.CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
        } else {
            req.header(HttpHeader.CONTENT_TYPE, FORM_URL_ENCODED + "; charset=" + req.postDataCharset());
        }
        return bound;
    }

    private static void writePost(final Connection.Request req, final OutputStream outputStream, final String bound) throws IOException {
        final Collection<Connection.KeyVal> data = req.data();
        final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(outputStream, req.postDataCharset()));

        if (bound != null) {
            // boundary will be set if we're in multipart mode
            for (Connection.KeyVal keyVal : data) {
                w.write("--");
                w.write(bound);
                w.write("\r\n");
                w.write("Content-Disposition: form-data; name=\"");
                w.write(encodeMimeName(keyVal.key())); // encodes " to %22
                w.write("\"");
                if (keyVal.hasInputStream()) {
                    w.write("; filename=\"");
                    w.write(encodeMimeName(keyVal.value()));
                    w.write("\"\r\nContent-Type: ");
                    w.write(keyVal.contentType() != null ? keyVal.contentType() : DefaultUploadType);
                    w.write("\r\n\r\n");
                    w.flush(); // flush
                    DataUtil.crossStreams(keyVal.inputStream(), outputStream);
                    outputStream.flush();
                } else {
                    w.write("\r\n\r\n");
                    w.write(keyVal.value());
                }
                w.write("\r\n");
            }
            w.write("--");
            w.write(bound);
            w.write("--");
        } else if (req.requestBody() != null) {
            // data will be in query string, we're sending a plaintext body
            w.write(req.requestBody());
        }
        else {
            // regular form data (application/x-www-form-urlencoded)
            boolean first = true;
            for (Connection.KeyVal keyVal : data) {
                if (!first)
                    w.append('&');
                else
                    first = false;

                w.write(URLEncoder.encode(keyVal.key(), req.postDataCharset()));
                w.write('=');
                w.write(URLEncoder.encode(keyVal.value(), req.postDataCharset()));
            }
        }
        w.close();
    }

    private static String getRequestCookieString(Connection.Request req) {
        StringBuilder sb = StringUtil.borrowBuilder();
        boolean first = true;
        for (Map.Entry<String, String> cookie : req.cookies().entrySet()) {
            if (!first)
                sb.append("; ");
            else
                first = false;
            sb.append(cookie.getKey()).append('=').append(cookie.getValue());
            // todo: spec says only ascii, no escaping / encoding defined. validate on set? or escape somehow here?
        }
        return StringUtil.releaseBuilder(sb);
    }

    // for get url reqs, serialise the data map into the url
    private static void serialiseRequestUrl(Connection.Request req) throws IOException {
        URL in = req.url();
        StringBuilder url = StringUtil.borrowBuilder();
        boolean first = true;
        // reconstitute the query, ready for appends
        url
                .append(in.getProtocol())
                .append("://")
                .append(in.getAuthority()) // includes host, port
                .append(in.getPath())
                .append("?");
        if (in.getQuery() != null) {
            url.append(in.getQuery());
            first = false;
        }
        for (Connection.KeyVal keyVal : req.data()) {
            Validate.isFalse(keyVal.hasInputStream(), "InputStream data not supported in URL query string.");
            if (!first)
                url.append('&');
            else
                first = false;
            url
                    .append(URLEncoder.encode(keyVal.key(), DataUtil.defaultCharset))
                    .append('=')
                    .append(URLEncoder.encode(keyVal.value(), DataUtil.defaultCharset));
        }
        req.url(new URL(StringUtil.releaseBuilder(url)));
        req.data().clear(); // moved into url as get params
    }

    private static String encodeMimeName(String val) {
        if (val == null)
            return null;
        return val.replaceAll("\"", "%22");
    }

    private static boolean needsMultipart(Connection.Request req) {
        // multipart mode, for files. add the header if we see something with an inputstream, and return a non-null boundary
        for (Connection.KeyVal keyVal : req.data()) {
            if (keyVal.hasInputStream())
                return true;
        }
        return false;
    }

}
