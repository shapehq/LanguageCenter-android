package com.novasa.languagecenter.retrofit;

import com.novasa.languagecenter.BuildConfig;

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
 *
 */
public class LCRestClient {

    private static final String TAG = "LC RetroFit";

    private LCApiService apiService;

    public LCRestClient(String url, String username, String password) {

        instantiate(url, username, password);

    }

    private void instantiate(final String url, final String username, final String password) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        if (BuildConfig.DEBUG){
            httpClient.addInterceptor(new DebugInterceptor());
        }

        // add basic credentials
        httpClient.authenticator(new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                String credential = Credentials.basic(username, password);
                return response.request().newBuilder().header("Authorization", credential).build();
            }

        });

        OkHttpClient client = httpClient.build();

        Retrofit builder = new Retrofit.Builder().client(client).baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();

        apiService = builder.create(LCApiService.class);

    }

    public LCApiService getApiService() {
        return apiService;

    }
}
