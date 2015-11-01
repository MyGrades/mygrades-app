package de.mygrades.main.rest;

import android.content.Context;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import de.mygrades.util.Config;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * RestClient encapsulates all REST interfaces from Retrofit.
 */
public class RestClient {
    private static final String TAG = RestClient.class.getSimpleName();

    private Context context;
    private RestApi restApi;

    public RestClient(Context context) {
        this.context = context.getApplicationContext();

        RequestInterceptor interceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Authorization", "Basic " + Config.getApiCredentials());
            }
        };

        // initialize GSON converter
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
                .registerTypeAdapter(boolean.class, booleanAsIntAdapter)
                .create();

        // initialize RestAdapter
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(Config.getServerUrl())
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(interceptor)
                .build();

        // create rest api
        restApi = restAdapter.create(RestApi.class);
    }

    public RestApi getRestApi() {
        return restApi;
    }

    /**
     * Custom TypeAdapter to convert values from JSON to boolean.
     *
     * "1", true, 1 => true
     * everything else => false
     */
    private static final TypeAdapter<Boolean> booleanAsIntAdapter = new TypeAdapter<Boolean>() {
        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            switch (peek) {
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                case NUMBER:
                    return in.nextInt() == 1;
                case STRING:
                    return in.nextString().equalsIgnoreCase("1");
                default:
                    Log.d(TAG, "Expected BOOLEAN, NUMBER or STRING but was " + peek);
                    return false;
            }
        }
    };
}
