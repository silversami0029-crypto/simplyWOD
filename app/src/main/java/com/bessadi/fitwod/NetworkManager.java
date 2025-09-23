package com.bessadi.fitwod;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkManager {
    private static NetworkManager instance;
    private OkHttpClient client;
    private Map<String, Call> ongoingCalls = new ConcurrentHashMap<>();

    private NetworkManager() {
        setupHttpClient();
    }

    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    private void setupHttpClient() {
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.MINUTES);

        client = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor())
                .build();
    }

    public void makeRequest(String url, Callback callback) {
        // Cancel previous call with same URL
        cancelRequest(url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        ongoingCalls.put(url, call);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ongoingCalls.remove(url);
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ongoingCalls.remove(url);
                callback.onResponse(call, response);
            }
        });
    }

    public void cancelRequest(String url) {
        Call previousCall = ongoingCalls.remove(url);
        if (previousCall != null) {
            previousCall.cancel();
        }
    }

    public void cancelAllRequests() {
        for (Call call : ongoingCalls.values()) {
            call.cancel();
        }
        ongoingCalls.clear();
    }

    public void shutdown() {
        cancelAllRequests();
        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
        instance = null;
    }

    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Log.d("Network", "Request: " + request.url());

            long startTime = System.nanoTime();
            Response response = chain.proceed(request);
            long elapsedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            Log.d("Network", String.format("Response: %s in %dms",
                    response.code(), elapsedTime));

            return response;
        }
    }
}
