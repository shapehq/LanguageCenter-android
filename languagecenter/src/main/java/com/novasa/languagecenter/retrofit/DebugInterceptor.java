package com.novasa.languagecenter.retrofit;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import timber.log.Timber;

/**
 * Created by mikkelschlager on 07/12/2016.
 */

@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class DebugInterceptor implements Interceptor {

    private boolean mPrintRequestBody;
    private boolean mPrintResponseBody;

    public DebugInterceptor() {
        mPrintRequestBody = true;
        mPrintResponseBody = true;
    }

    @SuppressWarnings("unused")
    public void setPrintRequestBody(boolean printRequestBody) {
        mPrintRequestBody = printRequestBody;
    }

    @SuppressWarnings("unused")
    public void setPrintResponseBody(boolean printResponseBody) {
        mPrintResponseBody = printResponseBody;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        final Request request = chain.request();

        final String url = request.url().toString();
        final String method = request.method();
        printBody(url, method, request.headers(), request.body());

        final Response response = chain.proceed(request);
        printBody(url, method, response.code(), response.receivedResponseAtMillis() - response.sentRequestAtMillis(), response.headers(), response.body());

        return response;
    }

    private void printBody(final String url, final String method, final Headers headers, final RequestBody body) {

        String bodyString = "-";
        if (mPrintRequestBody) {
            try {

                if (body != null) {

                    final Buffer buffer = new Buffer();
                    body.writeTo(buffer);
                    bodyString = buffer.readUtf8();
                } else {
                    bodyString = "none";
                }

            } catch (IOException e) {
                Timber.e(e);
            }
        }

        Timber.d("[REQUEST] METHOD: %s, URL: %s\nHEADERS: %sBODY: %s\n---", method, url, headers.toString(), bodyString);
    }

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private void printBody(final String url, final String method, final int status, final long time, final Headers headers, final ResponseBody body) {

        String bodyString = "-";
        if (mPrintResponseBody) {
            if (body != null) {
                try {
                    // Log the response body without consuming it
                    final BufferedSource source = body.source();

                    // Buffer the entire body.
                    source.request(Long.MAX_VALUE);
                    final Buffer buffer = source.buffer();

                    Charset charset = null;
                    final MediaType contentType = body.contentType();

                    if (contentType != null) {
                        try {
                            charset = contentType.charset(UTF8);
                        } catch (UnsupportedCharsetException e) {
                            Timber.e("Couldn't decode the response body; charset is likely malformed.");
                        }
                    }

                    if (charset == null) {
                        charset = UTF8;
                    }

                    bodyString = buffer.clone().readString(charset);

                } catch (IOException e) {
                    Timber.e(e);
                }

            } else {
                bodyString = "none";
            }
        }

        Timber.d("[RESPONSE] METHOD: %s, STATUS: %d, TIME: %d ms, URL: %s\nHEADERS: %sBODY: %s\n---", method, status, time, url, headers.toString(), bodyString);
    }
}
