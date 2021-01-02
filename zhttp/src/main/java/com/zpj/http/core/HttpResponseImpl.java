package com.zpj.http.core;

import android.util.Log;

import com.zpj.http.parser.html.utils.DataUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpResponseImpl extends HttpResponse {

    private HttpURLConnection conn;

    protected HttpResponseImpl(HttpRequest req) {
        super(req);

    }

    @Override
    protected ResponseInfo onExecute(HttpConfig config) throws Exception {
        String mimeBoundary = null;
        if (config.method().hasBody()) {
            Log.d("HttpResponse", "onExecute config.method.hasBody()=" + config.method.hasBody());
            if (config.method.hasBody())
                mimeBoundary = setOutputContentType();
        }

        conn = ConnectionFactory.createConnection(config);
        if (conn.getDoOutput()) {
            conn.setUseCaches(false);
            writePost2(conn, mimeBoundary);
        } else {
            conn.connect();
        }

//        conn = ConnectionFactory.createConnection(config);
//        Log.d("HttpResponse", "onExecute conn.getDoOutput()=" + conn.getDoOutput());
//        if (conn.getDoOutput()) {
//            conn.setUseCaches(false);
//            String mimeBoundary = null;
//            Log.d("HttpResponse", "onExecute config.method.hasBody()=" + config.method.hasBody());
//            if (config.method.hasBody())
//                mimeBoundary = setOutputContentType();
//            Log.d("HttpResponse", "onExecute mimeBoundary=" + mimeBoundary);
//            writePost2(conn, mimeBoundary);
//        } else {
//            conn.connect();
//        }

        long length;
        try {
            length = Long.parseLong(conn.getHeaderField(HttpHeader.CONTENT_LENGTH));
        } catch (Exception ignore) {
            length = conn.getContentLength();
        }
        Log.d("onExecute", "length=" + length + " content-length=" + conn.getContentLength());
        Map<String, List<String>> map = conn.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            Log.d("onExecute", "key=" + entry.getKey() + " value=" + entry.getValue());
        }
        return ResponseInfo.build()
                .setStatusCode(conn.getResponseCode())
                .setStatusMessage(conn.getResponseMessage())
                .setContentType(conn.getContentType())
                .setContentLength(length) // Long.parseLong(conn.getHeaderField(HttpHeader.CONTENT_LENGTH))
                .setHeaders(getHeaderMap(conn))
                .onGetBodyStream(new ResponseInfo.Callback() {
                    @Override
                    public InputStream get() throws Exception {
                        return conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();
                    }
                });
    }

    @Override
    public void disconnect() {
        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
    }

    private Map<String, String> getHeaderMap(HttpURLConnection conn) {
        // the default sun impl of conn.getHeaderFields() returns header values out of order
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        int i = 0;
        while (true) {
            final String key = conn.getHeaderFieldKey(i);
            final String val = conn.getHeaderField(i);
            if (key == null && val == null)
                break;
            i++;
            if (key == null || val == null)
                continue; // skip http1.1 line

            headers.put(key, val);
        }
        return headers;
    }



    private long getTotalBytes(final String bound) throws IOException {
        String charset = config.postDataCharset();
        byte[] boundaryBytes = ("--" + bound + "\r\n").getBytes(charset);
        byte[] trailerBytes = ("--" + bound + "--").getBytes(charset);
        long total = 0;
        for (IHttp.KeyVal keyVal : config.data()) {
            total += boundaryBytes.length;
            String multipartHeader = ("Content-Disposition: form-data; name=\"" + encodeMimeName(keyVal.key()) + "\"");
            if (keyVal.hasInputStream()) {
                multipartHeader += ("; filename=\"" + encodeMimeName(keyVal.value()) + "\"\r\nContent-Type: ");
                multipartHeader += (keyVal.contentType() != null ? keyVal.contentType() : DefaultUploadType);
                multipartHeader += "\r\n\r\n";
                total += multipartHeader.getBytes(charset).length;
                if (keyVal.inputStream() instanceof FileInputStream) {
                    total += ((FileInputStream) keyVal.inputStream()).getChannel().size();
                } else {
                    int available = keyVal.inputStream().available();
                    if (available < Integer.MAX_VALUE) {
                        total += available;
                    } else {
                        byte[] buf = new byte[512 * 1024];
                        int len;
                        while ((len = keyVal.inputStream().read(buf)) > 0) {
                            total += len;
                        }
                    }
                }
            } else {
                multipartHeader += ("\r\n\r\n" + keyVal.value());
                total += multipartHeader.getBytes(charset).length;
            }
            total += "\r\n".getBytes(charset).length;
        }
        total += trailerBytes.length;
        Log.d("HttpResponse", "total=" + total);
        return total;
    }

    private void writePost2(final HttpURLConnection conn, final String bound) throws IOException {
        final Collection<IHttp.KeyVal> data = config.data();
        String charset = config.postDataCharset();

//        final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(outputStream, req.postDataCharset()));

        OutputStream w;
        if (bound != null) {
            conn.setFixedLengthStreamingMode(getTotalBytes(bound));
            Log.d("HttpResponse", "setFixedLengthStreamingMode finished");
            w = conn.getOutputStream();
            // boundary will be set if we're in multipart mode
            byte[] boundaryBytes = ("--" + bound + "\r\n").getBytes(charset);
            byte[] trailerBytes = ("--" + bound + "--").getBytes(charset);
            for (IHttp.KeyVal keyVal : data) {
                w.write(boundaryBytes);
                String multipartHeader = "Content-Disposition: form-data; name=\"" + encodeMimeName(keyVal.key()) + "\"";
                if (keyVal.hasInputStream()) {

                    multipartHeader += "; filename=\"" + encodeMimeName(keyVal.value()) + "\"\r\nContent-Type: ";
                    multipartHeader += keyVal.contentType() != null ? keyVal.contentType() : DefaultUploadType;
                    multipartHeader += "\r\n\r\n";
                    w.write(multipartHeader.getBytes(charset));

                    Log.d("HttpResponse", "crossStreams");
                    DataUtil.crossStreams(keyVal.inputStream(), w, keyVal.getListener());
                    w.flush();
                } else {
                    multipartHeader += ("\r\n\r\n" + keyVal.value());
                    w.write(multipartHeader.getBytes(charset));
                }
                w.write("\r\n".getBytes(charset));
            }
            w.write(trailerBytes);
        } else if (config.requestBody() != null) {
            w = conn.getOutputStream();
            // data will be in query string, we're sending a plaintext body
            w.write(config.requestBody().getBytes(charset));
        } else {
            w = conn.getOutputStream();
            // regular form data (application/x-www-form-urlencoded)
            boolean first = true;
            for (IHttp.KeyVal keyVal : data) {
                if (!first)
                    w.write("&".getBytes(charset));
                else
                    first = false;

                w.write(URLEncoder.encode(keyVal.key(), config.postDataCharset()).getBytes(charset));
                w.write("=".getBytes(charset));
                w.write(URLEncoder.encode(keyVal.value(), config.postDataCharset()).getBytes(charset));
            }
        }
        w.close();
    }

    private static String encodeMimeName(String val) {
        if (val == null)
            return null;
        return val.replaceAll("\"", "%22");
    }

}
