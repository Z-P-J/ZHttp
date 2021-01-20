package com.zpj.http.core;

import com.zpj.http.exception.UncheckedIOException;
import com.zpj.http.parser.html.Parser;
import com.zpj.http.parser.html.nodes.Document;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import io.reactivex.disposables.Disposable;

public interface IHttp {

    /**
     * GET and POST http methods.
     */
    enum Method {

        GET(false), POST(true),
        PUT(true), DELETE(false),
        PATCH(true), HEAD(false),
        OPTIONS(false), TRACE(false);

        private final boolean hasBody;

        Method(boolean hasBody) {
            this.hasBody = hasBody;
        }

        /**
         * Check if this HTTP method has/needs a request body
         * @return if body needed
         */
        public final boolean hasBody() {
            return hasBody;
        }
    }

    interface OnRedirectListener {
        boolean onRedirect(int redirectCount, String redirectUrl);
    }

    interface OnErrorListener {
        void onError(Throwable throwable);
    }

    interface OnSuccessListener<T> {
        void onSuccess(T data) throws Exception;
    }

    interface OnCompleteListener {
        void onComplete() throws Exception;
    }

    interface OnSubscribeListener {
        void onSubscribe(Disposable d) throws Exception;
    }

    interface OnStreamWriteListener {
        /**
         * Called every time that a bunch of bytes were written to the body
         * @param bytesWritten number of written bytes
         */
        void onBytesWritten(int bytesWritten);

        boolean shouldContinue();
    }


    /**
     * Represents a HTTP request.
     */
    interface Request {

        Response response() throws Exception;

        Response syncExecute() throws Exception;

        String syncToStr() throws Exception;

        Document syncToHtml() throws Exception;

        JSONObject syncToJsonObject() throws Exception;

        JSONArray syncToJsonArray() throws Exception;

        Document syncToXml() throws Exception;


        HttpObserver<Response> execute();

        HttpObserver<String> toStr();

        HttpObserver<Document> toHtml();

        HttpObserver<JSONObject> toJsonObject();

        HttpObserver<JSONArray> toJsonArray();

        HttpObserver<Document> toXml();


    }

    /**
     * Represents a HTTP response.
     */
    interface Response {

        HttpConfig getConfig();

        Response execute() throws Exception;

        boolean hasHeader(String name);

        boolean hasHeaderWithValue(String name, String value);

        String header(String name);

        Map<String, String> headers();

        Map<String, String> cookies();

        String cookieStr();

        String cookie(String key);

        Method method();

        int statusCode();

        String statusMessage();

        String charset();

        String contentType();

        long contentLength();

//        Document parse(Parser parser) throws IOException;

        String body();

        /**
         * Get the body of the response as an array of bytes.
         * @return body bytes
         */
        byte[] bodyAsBytes();

        /**
         * Read the body of the response into a local buffer, so that {@link #parse()} may be called repeatedly on the
         * same connection response (otherwise, once the response is read, its InputStream will have been drained and
         * may not be re-read). Calling {@link #body() } or {@link #bodyAsBytes()} has the same effect.
         * @return this response, for chaining
         * @throws UncheckedIOException if an IO exception occurs during buffering.
         */
        Response bufferUp();

        /**
         * Get the body of the response as a (buffered) InputStream. You should close the input stream when you're done with it.
         * Other body methods (like bufferUp, body, parse, etc) will not work in conjunction with this method.
         * <p>This method is useful for writing large responses to disk, without buffering them completely into memory first.</p>
         * @return the response body input stream
         */
        BufferedInputStream bodyStream();

        void close();

        void disconnect();

        void closeIO();
    }

    /**
     * A Key:Value tuple(+), used for form data.
     */
    interface KeyVal {

        /**
         * Update the key of a keyval
         * @param key new key
         * @return this KeyVal, for chaining
         */
        IHttp.KeyVal key(String key);

        /**
         * Get the key of a keyval
         * @return the key
         */
        String key();

        /**
         * Update the value of a keyval
         * @param value the new value
         * @return this KeyVal, for chaining
         */
        IHttp.KeyVal value(String value);

        /**
         * Get the value of a keyval
         * @return the value
         */
        String value();

        /**
         * Add or update an input stream to this keyVal
         * @param inputStream new input stream
         * @return this KeyVal, for chaining
         */
        IHttp.KeyVal inputStream(InputStream inputStream);

        /**
         * Get the input stream associated with this keyval, if any
         * @return input stream if set, or null
         */
        InputStream inputStream();

        /**
         * Does this keyval have an input stream?
         * @return true if this keyval does indeed have an input stream
         */
        boolean hasInputStream();

        /**
         * Set the Content Type header used in the MIME body (aka mimetype) when uploading files.
         * Only useful if {@link #inputStream(InputStream)} is set.
         * <p>Will default to {@code application/octet-stream}.</p>
         * @param contentType the new content type
         * @return this KeyVal
         */
        IHttp.KeyVal contentType(String contentType);

        /**
         * Get the current Content Type, or {@code null} if not set.
         * @return the current Content Type.
         */
        String contentType();

        IHttp.KeyVal setListener(IHttp.OnStreamWriteListener listener);

        IHttp.OnStreamWriteListener getListener();

    }

}
