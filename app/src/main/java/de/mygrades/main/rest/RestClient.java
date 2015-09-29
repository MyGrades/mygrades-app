package de.mygrades.main.rest;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.mygrades.util.Config;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * RestClient encapsulates all REST interfaces from Retrofit.
 */
public class RestClient {
    private Context context;
    private RestApi restApi;

    public RestClient(Context context) {
        this.context = context.getApplicationContext();

        RequestInterceptor interceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Authorization", "Basic " + Config.API_BASE64_CREDENTIALS);
            }
        };

        // initialize GSON converter
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        // initialize RestAdapter
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(Config.SERVER_URL)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(interceptor)
                .build();

        // create rest api
        restApi = restAdapter.create(RestApi.class);
    }

    public RestApi getRestApi() {
        return restApi;
    }
}
