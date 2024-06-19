package com.shaihi.gpt_example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView setupTextView;
    private TextView resultTextView;
    private EditText inputEditText;
    private OpenAIClient apiClient;
    private List<JSONObject> conversationHistory;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupTextView = findViewById(R.id.setupTextView);
        resultTextView = findViewById(R.id.resultTextView);
        inputEditText = findViewById(R.id.inputEditText);
        Button sendButton = findViewById(R.id.sendButton);

        apiClient = new OpenAIClient(getString(R.string.openai_api_key), this); // Replace with your API key
        conversationHistory = new ArrayList<>();
        executor = Executors.newSingleThreadExecutor();

        String setupRole = ""; // Your initial setup prompt if any

        try {
            // Initialize conversation history with setup prompt
            conversationHistory.add(new JSONObject().put("role", "system").put("content", setupRole));
        } catch (JSONException e) {
            Log.e(TAG, "Error adding setup prompt to conversation history", e);
        }

        // Display the setup prompt result
        setupTextView.setText(setupRole);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = inputEditText.getText().toString();
                fetchCompletion(userInput);
            }
        });
    }

    private void fetchCompletion(String prompt) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create a new conversation history for each request
                    List<JSONObject> currentConversationHistory = new ArrayList<>();
                    currentConversationHistory.addAll(conversationHistory);

                    // Add user message to the current conversation history
                    currentConversationHistory.add(new JSONObject()
                            .put("role", "user")
                            .put("content", prompt));

                    // Call getCompletion with the current conversation history
                    String response = apiClient.getCompletion(prompt, currentConversationHistory);
                    String parsedResponse = parseResponse(response);

                    // Update UI on the main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Update the main conversation history
                                conversationHistory.add(new JSONObject()
                                        .put("role", "user")
                                        .put("content", prompt));
                                conversationHistory.add(new JSONObject()
                                        .put("role", "assistant")
                                        .put("content", parsedResponse));
                                resultTextView.setText(parsedResponse);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error adding response to conversation history", e);
                                resultTextView.setText("Error: " + e.getMessage());
                            }
                        }
                    });
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Error during API call", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultTextView.setText("Error: " + e.getMessage());
                        }
                    });
                }
            }

            private String parseResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray choicesArray = jsonObject.getJSONArray("choices");
                    JSONObject choice = choicesArray.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    return message.getString("content");
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON response", e);
                    return "Error parsing response";
                }
            }
        });
    }
}