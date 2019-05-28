package com.novasa.languagecenter.service;

import com.novasa.languagecenter.LanguageCenter;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by andersp on 28/09/16.
 */
public final class LCRestClient {

    private final LCApiService mService;
    private DebugInterceptor mDebugInterceptor;

    public LCRestClient(final String url, final String username, final String password) {

        final OkHttpClient.Builder http = new OkHttpClient.Builder()
                .authenticator(new Auth(username, password));

        if (LanguageCenter.DEBUGGABLE) {
            http.addInterceptor(mDebugInterceptor = new DebugInterceptor());
            setDebugMode(false);
        }

        final Retrofit builder = new Retrofit.Builder()
                .client(http.build())
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mService = builder.create(LCApiService.class);
    }

    public LCApiService getApiService() {
        return mService;
    }

    public void setDebugMode(boolean debug) {
        if (mDebugInterceptor != null) {
            mDebugInterceptor.setPrintRequestBody(debug)
                    .setPrintRequestHeaders(debug)
                    .setPrintResponseHeaders(debug)
                    .setPrintResponseBody(debug);
        }
    }

    private static class Auth implements Authenticator {

        private final String mUsername;
        private final String mPassword;

        Auth(String mUsername, String mPassword) {
            this.mUsername = mUsername;
            this.mPassword = mPassword;
        }

        @Override
        public Request authenticate(Route route, Response response) throws IOException {

            // Add basic credentials
            final String credential = Credentials.basic(mUsername, mPassword);
            return response.request()
                    .newBuilder()
                    .header("Authorization", credential)
                    .build();
        }
    }
}
