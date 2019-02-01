package com.novasa.languagecenter.service;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.novasa.languagecenter.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by mikkelschlager on 07/12/2016.
 */
@SuppressWarnings({"WeakerAccess", "SameParameterValue", "unused", "DefaultLocale"})
public class DebugInterceptor implements Interceptor {

    private boolean mPrintRequestHeaders = true;
    private boolean mPrintRequestBody = true;
    private boolean mPrintResponseHeaders = true;
    private boolean mPrintResponseBody = false;
    private boolean mPrintResponseErrorBody = true;

    private String[] mPrintRequestBodyParamsInResponse;
    private Gson mGson;

    /** Default = true */
    public DebugInterceptor setPrintRequestHeaders(boolean print) {
        mPrintRequestHeaders = print;
        return this;
    }

    /** Default = true */
    public DebugInterceptor setPrintRequestBody(boolean print) {
        mPrintRequestBody = print;
        return this;
    }

    /** Default = true */
    public DebugInterceptor setPrintResponseHeaders(boolean print) {
        mPrintResponseHeaders = print;
        return this;
    }

    /** Default = false */
    public DebugInterceptor setPrintResponseBody(boolean print) {
        mPrintResponseBody = print;
        return this;
    }

    /** Default = true */
    public DebugInterceptor setPrintResponseErrorBody(boolean print) {
        mPrintResponseErrorBody = print;
        return this;
    }

    /** Example use: If the rest method name is in the request body, and you want to print it in the response. */
    public DebugInterceptor setPrintRequestBodyParamsInResponse(String... printRequestBodyParamsInResponse) {
        mPrintRequestBodyParamsInResponse = printRequestBodyParamsInResponse;
        mGson = new Gson();
        return this;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        final Request request = chain.request();

        printRequest(request);

        final Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            printError(request, e);
            throw e;
        }

        printResponse(response);

        return response;
    }

    private void printRequest(final Request request) {

        final StringBuilder sb = new StringBuilder("[REQUEST]");

        sb.append(String.format(" | METHOD: %s", request.method()));
        sb.append(String.format(" | URL: %s", request.url().toString()));

        if (mPrintRequestHeaders) {
            sb.append(String.format("\n| HEADERS: %s", headersToString(request.headers())));
        }

        if (mPrintRequestBody) {
            sb.append(String.format("\n| BODY: %s", parseRequestBody(request.body())));
        }

        Logger.d(sb.toString());
    }


    private final static Charset UTF8 = Charset.forName("UTF-8");

    private void printResponse(final Response response) {

        final StringBuilder sb = new StringBuilder("[RESPONSE]");

        sb.append(String.format(" | METHOD: %s", response.request().method()));
        sb.append(String.format(" | URL: %s", response.request().url().toString()));
        sb.append(String.format(" | STATUS: %d (%s)", response.code(), response.message()));
        sb.append(String.format(" | TIME: %d ms", response.receivedResponseAtMillis() - response.sentRequestAtMillis()));

        if (mPrintRequestBodyParamsInResponse != null) {
            try {
                final String requestBody = parseRequestBody(response.request().body());
                final Map params = mGson.fromJson(requestBody, Map.class);
                for (final String p : mPrintRequestBodyParamsInResponse) {
                    if (params.containsKey(p)) {
                        final Object v = params.get(p);
                        sb.append(String.format(" | %s: %s", p, v));
                    }
                }
            } catch (Exception e) {
                Logger.e(e);
            }
        }

        if (mPrintResponseHeaders) {
            sb.append(String.format("\n| HEADERS: %s", headersToString(response.headers())));
        }

        final boolean success = response.isSuccessful();

        if (success && mPrintResponseBody || !success && mPrintResponseErrorBody) {
            sb.append(String.format("\n| BODY: %s", parseResponseBody(response.body())));
        }

        if (success) {
            Logger.d(sb.toString());

        } else {
            Logger.e(sb.toString());
        }
    }

    private void printError(final Request request, final Exception e) {
        final StringBuilder sb = new StringBuilder("[ERROR]");
        sb.append(String.format(" | METHOD: %s", request.method()));
        sb.append(String.format(" | URL: %s", request.url().toString()));
        sb.append(String.format(" | EXCEPTION: %s", e.getClass().getName()));
        sb.append(String.format(" | MESSAGE: %s", e.getMessage()));

        Logger.e(sb.toString());
    }

    private String headersToString(Headers headers) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, size = headers.size(); i < size; i++) {
            sb.append("\n|   ").append(headers.name(i)).append(": ").append(headers.value(i));
        }
        return sb.toString();
    }

    private String parseRequestBody(RequestBody body) {
        try {
            final String bodyString;

            if (body != null) {
                final Buffer buffer = new Buffer();
                body.writeTo(buffer);
                return buffer.readUtf8();

            } else {
                return "none";
            }

        } catch (Exception e) {
            Logger.e(e);
        }

        return null;
    }

    private String parseResponseBody(ResponseBody body) {
        try {
            if (body != null) {

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
                        Logger.e("Couldn't decode the response body; charset is likely malformed.");
                    }
                }

                if (charset == null) {
                    charset = UTF8;
                }

                final Buffer clone = buffer.clone();

                return String.format("size: %d bytes, content: %s", clone.size(), clone.readString(charset));

            } else {
                return "none";
            }

        } catch (Exception e) {
            Logger.e(e);
        }

        return null;
    }
}
