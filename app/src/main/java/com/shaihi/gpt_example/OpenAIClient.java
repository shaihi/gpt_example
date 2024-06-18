package com.shaihi.gpt_example;

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

    public OpenAIClient(String apiKey) {
        this.client = new OkHttpClient();
        this.apiKey = apiKey;
    }

    public String getCompletion(String prompt, List<JSONObject> conversationHistory) throws IOException {
        try {
            JSONArray messages = new JSONArray(conversationHistory);
            messages.put(new JSONObject().put("role", "user").put("content", prompt));

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", messages);

            RequestBody body = RequestBody.create(requestBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(OPENAI_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            Log.d(TAG, "Request: " + request.toString());
            Log.d(TAG, "Request Body: " + requestBody.toString());

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unexpected code " + response);
                    throw new IOException("Unexpected code " + response);
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