package com.shaihi.gpt_example;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAIClient {
    private static final String TAG = "OpenAIClient";
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final String apiKey;
    private Context context;

    public OpenAIClient(String apiKey, Context context) {
        this.client = new OkHttpClient();
        this.apiKey = apiKey;
        this.context = context;
    }

    public String getCompletion(String prompt, List<JSONObject> conversationHistory) throws IOException {
        try {
            JSONArray messages = new JSONArray();
            for (JSONObject message : conversationHistory) {
                messages.put(message);
            }
            messages.put(new JSONObject().put("role", "user").put("content", prompt));

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", context.getString(R.string.gpt_model)); // Or your preferred model
            requestBody.put("messages", messages);

            RequestBody body = RequestBody.create(requestBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(OPENAI_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            Log.d(TAG, "Request: " + request.toString());
            Log.d(TAG,"Request Body: " + requestBody.toString());

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    Log.e(TAG, "Unexpected code " + response + ", Error Body: " + errorBody);
                    throw new IOException("Unexpected code " + response + ", Error Body: " + errorBody);
                }
                String responseBody = response.body().string();
                Log.d(TAG, "Response: " + responseBody);
                return responseBody;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error constructing request", e);
            throw new IOException("Error constructing request", e);
        }
    }
}