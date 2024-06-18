package com.shaihi.gpt_example;
import android.os.AsyncTask;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView setupTextView;
    private TextView resultTextView;
    private EditText inputEditText;
    private OpenAIClient apiClient;
    private List<JSONObject> conversationHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupTextView = findViewById(R.id.setupTextView);
        resultTextView = findViewById(R.id.resultTextView);
        inputEditText = findViewById(R.id.inputEditText);
        Button sendButton = findViewById(R.id.sendButton);

        apiClient = new OpenAIClient(getString(R.string.openai_api_key));
        conversationHistory = new ArrayList<>();

        String setupRole = "You screen bad language. If inappropriate language is used, " +
                "you respond with a single word: NAUGHTY!. " +
                "Otherwise you respond as a helpful assistant.";

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
                new FetchCompletionTask().execute(userInput);
            }
        });
    }

    private class FetchCompletionTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... prompts) {
            try {
                // Add user message to conversation history
                conversationHistory.add(new JSONObject().put("role", "user").put("content", prompts[0]));
                String response = apiClient.getCompletion(prompts[0], conversationHistory);
                return parseResponse(response);
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error during API call", e);
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                conversationHistory.add(new JSONObject().put("role", "assistant").put("content", result));
                resultTextView.setText(result);
            } catch (JSONException e) {
                Log.e(TAG, "Error adding response to conversation history", e);
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
    }
}