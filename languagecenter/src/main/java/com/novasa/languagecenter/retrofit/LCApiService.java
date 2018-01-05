package com.novasa.languagecenter.retrofit;


import com.novasa.languagecenter.model.Language;
import com.novasa.languagecenter.model.Translation;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by andersp on 28/09/16.
 *
 */
public interface LCApiService {

    // get list of translations
    @GET("strings")
    Call<List<Translation>> getTranslations(@Query("platform") String platform, @Query("language") String languagecode, @Query("indexing") String indexing, @Query("timestamp") String timestamp);

    // get list of available languages
    @GET("languages")
    Call<List<Language>> getLanguages(@Query("timestamp") String timestamp);

    // get info on a single language
    @GET("language/{languageCode}")
    Call<Language> getLanguage(@Path("languageCode") String languageCode);

    // submit new translation
    @FormUrlEncoded
    @POST("string")
    Call<Translation> createTranslation(@Field("platform") String platform, @Field("category") String category, @Field("key") String key, @Field("value") String value, @Field("comment") String comment);

}
