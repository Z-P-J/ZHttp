//package com.zpj.http.core;
//
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.zpj.http.parser.html.utils.DataUtil;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URLEncoder;
//import java.security.KeyManagementException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.X509Certificate;
//import java.util.Collection;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSession;
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//
//public class HttpURLEngine implements IHttp.HttpEngine {
//
//    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
//    public static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
//    public static final String DefaultUploadType = "application/octet-stream";
//
//
//    @Override
//    public ResponseInfo execute(IHttp.Request req) throws Exception {
//        HttpConfig config = req.config();
//        String mimeBoundary = null;
//        if (req.config().method().hasBody()) {
//            mimeBoundary = setOutputContentType(req);
//        }
//
//        final HttpURLConnection conn = createConnection(req);
//        if (conn.getDoOutput()) {
//            conn.setUseCaches(false);
//            writePost2(req, conn, mimeBoundary);
//        } else {
//            conn.connect();
//        }
//
//        long length;
//        try {
//            length = Long.parseLong(conn.getHeaderField(HttpHeader.CONTENT_LENGTH));
//        } catch (Exception ignore) {
//            length = conn.getContentLength();
//        }
//        Map<String, List<String>> map = conn.getHeaderFields();
//
//        if (req.debug()) {
//            Log.d("onExecute", "length=" + length + " content-length=" + conn.getContentLength());
//            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//                Log.d("onExecute", "key=" + entry.getKey() + " value=" + entry.getValue());
//            }
//        }
//
//        return ResponseInfo.build()
//                .setStatusCode(conn.getResponseCode())
//                .setStatusMessage(conn.getResponseMessage())
//                .setContentType(conn.getContentType())
//                .setContentLength(length)
//                .setHeaders(getHeaderMap(conn))
//                .onGetBodyStream(new ResponseInfo.Callback() {
//                    @Override
//                    public InputStream getBodyStream() throws Exception {
//                        return conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();
//                    }
//
//                    @Override
//                    public void disconnect() {
//                        if (conn != null) {
//                            conn.disconnect();
//                        }
//                    }
//                });
//    }
//
//    private Map<String, String> getHeaderMap(HttpURLConnection conn) {
//        // the default sun impl of conn.getHeaderFields() returns header values out of order
//        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
//        int i = 0;
//        while (true) {
//            final String key = conn.getHeaderFieldKey(i);
//            final String val = conn.getHeaderField(i);
//            if (key == null && val == null)
//                break;
//            i++;
//            if (key == null || val == null)
//                continue; // skip http1.1 line
//
//            headers.put(key, val);
//        }
//        return headers;
//    }
//
//    public HttpURLConnection createConnection(Request req) throws IOException {
//        final HttpURLConnection conn = (HttpURLConnection) (
//                req.proxy() == null ?
//                        req.url().openConnection() :
//                        req.url().openConnection(req.proxy())
//        );
//
//        conn.setRequestMethod(req.method().name());
//        conn.setInstanceFollowRedirects(false); // don't rely on native redirection support
//        conn.setConnectTimeout(req.connectTimeout());
//        conn.setReadTimeout(req.readTimeout()); // gets reduced after connection is made and status is read
//
//        if (conn instanceof HttpsURLConnection) {
//            SSLSocketFactory socketFactory = req.sslSocketFactory();
//
//            if (socketFactory != null) {
//                ((HttpsURLConnection) conn).setSSLSocketFactory(socketFactory);
//            } else if (req.allowAllSSL()) {
//                initUnSecureTSL(req);
//                ((HttpsURLConnection) conn).setSSLSocketFactory(req.sslSocketFactory());
//                ((HttpsURLConnection) conn).setHostnameVerifier(new HostnameVerifier() {
//                    public boolean verify(String urlHostName, SSLSession session) {
//                        return true;
//                    }
//                });
//            }
//        }
//
//        if (req.method().hasBody())
//            conn.setDoOutput(true);
//
//        if (req.cookieJar() != null) {
//            Map<String, String> cookieMap = req.cookieJar().loadCookies(req.url());
//            if (cookieMap != null) {
//                for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
//                    if (!req.hasCookie(entry.getKey())) {
//                        req.cookie(entry.getKey(), entry.getValue());
//                    }
//                }
//            }
//        }
//
//
//        if (req.cookies().size() > 0) {
//            conn.addRequestProperty(HttpHeader.COOKIE, req.cookieStr());
//        }
//        if (!TextUtils.isEmpty(req.userAgent())) {
//            conn.addRequestProperty(HttpHeader.USER_AGENT, req.userAgent());
//        }
//        for (Map.Entry<String, String> header : req.headers().entrySet()) {
//            conn.addRequestProperty(header.getKey(), header.getValue());
//        }
//        return conn;
//    }
//
//    private void initUnSecureTSL(final Request req) throws IOException {
//        if (req.sslSocketFactory() == null) {
//            // Create a trust manager that does not validate certificate chains
//            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//
//                public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
//                }
//
//                public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
//                }
//
//                public X509Certificate[] getAcceptedIssuers() {
//                    return null;
//                }
//            }};
//
//            // Install the all-trusting trust manager
//            final SSLContext sslContext;
//            try {
//                sslContext = SSLContext.getInstance("SSL");
//                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//                // Create an ssl socket factory with our all-trusting manager
//                req.sslSocketFactory(sslContext.getSocketFactory());
//            } catch (NoSuchAlgorithmException | KeyManagementException e) {
//                throw new IOException("Can't create unsecure trust manager");
//            }
//        }
//    }
//
//    protected String setOutputContentType(Request req) {
//        String bound = null;
//        if (req.hasHeader(HttpHeader.CONTENT_TYPE)) {
//            // no-op; don't add content type as already set (e.g. for requestBody())
//            // todo - if content type already set, we could add charset
//
//            // if user has set content type to multipart/form-data, auto add boundary.
//            if (req.header(HttpHeader.CONTENT_TYPE).contains(MULTIPART_FORM_DATA) &&
//                    !req.header(HttpHeader.CONTENT_TYPE).contains("boundary")) {
//                bound = DataUtil.mimeBoundary();
//                req.header(HttpHeader.CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
//            }
//
//        } else if (req.needsMultipart()) {
//            bound = DataUtil.mimeBoundary();
//            req.header(HttpHeader.CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
//        } else {
//            req.header(HttpHeader.CONTENT_TYPE, FORM_URL_ENCODED + "; charset=" + req.postDataCharset());
//        }
//        return bound;
//    }
//
//    private void writePost2(final Request req, final HttpURLConnection conn, final String bound) throws IOException {
//        final Collection<IHttp.KeyVal> data = req.data();
//        String charset = req.postDataCharset();
//
////        final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(outputStream, req.postDataCharset()));
//
//        OutputStream w;
//        if (bound != null) {
//            conn.setFixedLengthStreamingMode(getTotalBytes(req, bound));
//            Log.d("HttpResponse", "setFixedLengthStreamingMode finished");
//            w = conn.getOutputStream();
//            // boundary will be set if we're in multipart mode
//            byte[] boundaryBytes = ("--" + bound + "\r\n").getBytes(charset);
//            byte[] trailerBytes = ("--" + bound + "--").getBytes(charset);
//            for (IHttp.KeyVal keyVal : data) {
//                w.write(boundaryBytes);
//                String multipartHeader = "Content-Disposition: form-data; name=\"" + encodeMimeName(keyVal.key()) + "\"";
//                if (keyVal.hasInputStream()) {
//
//                    multipartHeader += "; filename=\"" + encodeMimeName(keyVal.value()) + "\"\r\nContent-Type: ";
//                    multipartHeader += (TextUtils.isEmpty(keyVal.contentType()) ? DefaultUploadType : keyVal.contentType());
//                    multipartHeader += "\r\n\r\n";
//                    w.write(multipartHeader.getBytes(charset));
//
//                    Log.d("HttpResponse", "crossStreams");
//                    DataUtil.crossStreams(keyVal.inputStream(), w, keyVal.getListener());
//                    w.flush();
//                } else {
//                    multipartHeader += ("\r\n\r\n" + keyVal.value());
//                    w.write(multipartHeader.getBytes(charset));
//                }
//                w.write("\r\n".getBytes(charset));
//            }
//            w.write(trailerBytes);
//        } else if (req.requestBody() != null) {
//            w = conn.getOutputStream();
//            // data will be in query string, we're sending a plaintext body
//            w.write(req.requestBody().getBytes(charset));
//        } else {
//            w = conn.getOutputStream();
//            // regular form data (application/x-www-form-urlencoded)
//            boolean first = true;
//            for (IHttp.KeyVal keyVal : data) {
//                if (!first)
//                    w.write("&".getBytes(charset));
//                else
//                    first = false;
//
//                w.write(URLEncoder.encode(keyVal.key(), req.postDataCharset()).getBytes(charset));
//                w.write("=".getBytes(charset));
//                w.write(URLEncoder.encode(keyVal.value(), req.postDataCharset()).getBytes(charset));
//            }
//        }
//        w.close();
//    }
//
//    private long getTotalBytes(Request req, final String bound) throws IOException {
//        String charset = req.postDataCharset();
//        byte[] boundaryBytes = ("--" + bound + "\r\n").getBytes(charset);
//        byte[] trailerBytes = ("--" + bound + "--").getBytes(charset);
//        long total = 0;
//        for (IHttp.KeyVal keyVal : req.data()) {
//            total += boundaryBytes.length;
//            String multipartHeader = ("Content-Disposition: form-data; name=\"" + encodeMimeName(keyVal.key()) + "\"");
//            if (keyVal.hasInputStream()) {
//                multipartHeader += ("; filename=\"" + encodeMimeName(keyVal.value()) + "\"\r\nContent-Type: ");
//                multipartHeader += (TextUtils.isEmpty(keyVal.contentType()) ? DefaultUploadType : keyVal.contentType());
//                multipartHeader += "\r\n\r\n";
//                total += multipartHeader.getBytes(charset).length;
//                if (keyVal.inputStream() instanceof FileInputStream) {
//                    total += ((FileInputStream) keyVal.inputStream()).getChannel().size();
//                } else {
//                    int available = keyVal.inputStream().available();
//                    if (available < Integer.MAX_VALUE) {
//                        total += available;
//                    } else {
//                        byte[] buf = new byte[512 * 1024];
//                        int len;
//                        while ((len = keyVal.inputStream().read(buf)) > 0) {
//                            total += len;
//                        }
//                    }
//                }
//            } else {
//                multipartHeader += ("\r\n\r\n" + keyVal.value());
//                total += multipartHeader.getBytes(charset).length;
//            }
//            total += "\r\n".getBytes(charset).length;
//        }
//        total += trailerBytes.length;
//        Log.d("HttpResponse", "total=" + total);
//        return total;
//    }
//
//    private static String encodeMimeName(String val) {
//        if (val == null)
//            return null;
//        return val.replaceAll("\"", "%22");
//    }
//
//}
