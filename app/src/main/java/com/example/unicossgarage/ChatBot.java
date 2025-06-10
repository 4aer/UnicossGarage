package com.example.unicossgarage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatBot extends AppCompatActivity {

    private LinearLayout chatContainer;
    private ScrollView scrollView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton backButton;
    private LinearLayout bottomButtonsContainer;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ChatBotPrefs";
    private String MESSAGES_KEY; // Now dynamic based on user

    private List<ChatMessage> chatMessages;
    private Gson gson;
    private String currentUsername; // Store current logged-in user

    // Bottom inquiry buttons
    private Button btnServiceSchedule, btnModeOfPayment, btnVehicleType, btnLocation, btnOthers;

    private boolean isWaitingForOthersInput = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        // Get current username from Intent or SharedPreferences
        getCurrentUser();

        if (currentUsername == null) {
            // If no user is logged in, redirect to login
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initializeViews();
        setupSharedPreferences();
        loadChatHistory();
        setupClickListeners();

        // Show initial greeting if no messages exist
        if (chatMessages.isEmpty()) {
            showInitialGreeting();
        }
    }

    private void getCurrentUser() {
        // First try to get from Intent (if passed from login/register)
        currentUsername = getIntent().getStringExtra("username");

        // If not found in Intent, try to get from SharedPreferences (current session)
        if (currentUsername == null) {
            SharedPreferences sessionPrefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            currentUsername = sessionPrefs.getString("current_user", null);
        }

        // If we have a username, make sure it's stored in session
        if (currentUsername != null) {
            SharedPreferences sessionPrefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            sessionPrefs.edit().putString("current_user", currentUsername).apply();
        }
    }

    private void initializeViews() {
        chatContainer = findViewById(R.id.chatContainer);
        scrollView = findViewById(R.id.scrollView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        bottomButtonsContainer = findViewById(R.id.bottomButtonsContainer);

        btnServiceSchedule = findViewById(R.id.btnServiceSchedule);
        btnModeOfPayment = findViewById(R.id.btnModeOfPayment);
        btnVehicleType = findViewById(R.id.btnVehicleType);
        btnLocation = findViewById(R.id.btnLocation);
        btnOthers = findViewById(R.id.btnOthers);
    }

    private void setupSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        chatMessages = new ArrayList<>();

        // Create user-specific key for chat messages
        MESSAGES_KEY = "chat_messages_" + currentUsername;
    }

    private void loadChatHistory() {
        String messagesJson = sharedPreferences.getString(MESSAGES_KEY, "");
        if (!TextUtils.isEmpty(messagesJson)) {
            Type listType = new TypeToken<List<ChatMessage>>(){}.getType();
            chatMessages = gson.fromJson(messagesJson, listType);

            // Display loaded messages
            for (ChatMessage message : chatMessages) {
                displayMessage(message, false);
            }
        }
    }

    private void saveChatHistory() {
        String messagesJson = gson.toJson(chatMessages);
        sharedPreferences.edit().putString(MESSAGES_KEY, messagesJson).apply();
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> finish());

        btnServiceSchedule.setOnClickListener(v -> handleInquiryClick("Service Schedule"));
        btnModeOfPayment.setOnClickListener(v -> handleInquiryClick("Mode of Payment"));
        btnVehicleType.setOnClickListener(v -> handleInquiryClick("Accepted Vehicle Type"));
        btnLocation.setOnClickListener(v -> handleInquiryClick("Location"));
        btnOthers.setOnClickListener(v -> handleOthersClick());
    }

    // Add method to clear chat history (useful for logout)
    public void clearChatHistory() {
        chatMessages.clear();
        sharedPreferences.edit().remove(MESSAGES_KEY).apply();
        chatContainer.removeAllViews();
    }

    private void showInitialGreeting() {
        // Add date header
        addDateHeader();

        new Handler().postDelayed(() -> {
            String greeting = "Unicoss Garage is Here to Help! What can we do for you?";
            ChatMessage botMessage = new ChatMessage(greeting, false, getCurrentTimestamp());
            addMessageAndSave(botMessage);
            displayMessage(botMessage, true);
        }, 1000);
    }

    private void addDateHeader() {
        View dateHeaderView = getLayoutInflater().inflate(R.layout.date_header, chatContainer, false);
        TextView dateText = dateHeaderView.findViewById(R.id.dateText);

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        dateText.setText(sdf.format(new Date()));

        chatContainer.addView(dateHeaderView);
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(message)) return;

        // Add user message
        ChatMessage userMessage = new ChatMessage(message, true, getCurrentTimestamp());
        addMessageAndSave(userMessage);
        displayMessage(userMessage, true);

        messageInput.setText("");

        // Handle bot response
        handleBotResponse(message);
    }

    private void handleInquiryClick(String inquiry) {
        // Add user message
        ChatMessage userMessage = new ChatMessage(inquiry, true, getCurrentTimestamp());
        addMessageAndSave(userMessage);
        displayMessage(userMessage, true);

        // Generate bot response
        String botResponse = getBotResponse(inquiry);

        new Handler().postDelayed(() -> {
            ChatMessage botMessage = new ChatMessage(botResponse, false, getCurrentTimestamp());
            addMessageAndSave(botMessage);
            displayMessage(botMessage, true);
        }, 1000);
    }

    private void handleOthersClick() {
        String inquiry = "Others";
        ChatMessage userMessage = new ChatMessage(inquiry, true, getCurrentTimestamp());
        addMessageAndSave(userMessage);
        displayMessage(userMessage, true);

        new Handler().postDelayed(() -> {
            String botResponse = "Please state your inquiry, and we will respond to you as soon as possible.";
            ChatMessage botMessage = new ChatMessage(botResponse, false, getCurrentTimestamp());
            addMessageAndSave(botMessage);
            displayMessage(botMessage, true);

            isWaitingForOthersInput = true;
        }, 1000);
    }

    private void handleBotResponse(String userMessage) {
        String botResponse;

        if (isWaitingForOthersInput) {
            botResponse = "Thank you for sharing your inquiry. It will be reviewed by our admins.";
            isWaitingForOthersInput = false;
        } else {
            botResponse = "Please choose one of the inquiries indicated below.";
        }

        new Handler().postDelayed(() -> {
            ChatMessage botMessage = new ChatMessage(botResponse, false, getCurrentTimestamp());
            addMessageAndSave(botMessage);
            displayMessage(botMessage, true);
        }, 1000);
    }

    private String getBotResponse(String inquiry) {
        switch (inquiry) {
            case "Service Schedule":
                return "Our business hours are from Monday to Friday, 8:00 AM to 7:00 PM. Please note that we are closed on holidays.";
            case "Mode of Payment":
                return "We accept both online payments and cash. Payment can be made upon completion of your vehicle modifications.";
            case "Accepted Vehicle Type":
                return "We do not service trucks, motorcycles, and other two- or three-wheel vehicles. Our equipment, setup, and detailing process are optimized for standard consumer vehicles, particularly vans, SUVs, sedans, and similar-sized cars.";
            case "Location":
                return "We are located at Instruccion Street, Matimyas, Sampaloc, City Of Manila, Metro Manila.";
            default:
                return "Please choose one of the inquiries indicated above.";
        }
    }

    private void displayMessage(ChatMessage message, boolean animate) {
        View messageView = getLayoutInflater().inflate(
                message.isUser() ? R.layout.user_message : R.layout.chat_bot_message,
                chatContainer,
                false
        );

        TextView messageText = messageView.findViewById(R.id.messageText);
        messageText.setText(message.getText());

        chatContainer.addView(messageView);

        if (animate) {
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_message);
            messageView.startAnimation(slideIn);
        }

        // Scroll to bottom
        new Handler().postDelayed(() -> {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }, 100);
    }

    private void addMessageAndSave(ChatMessage message) {
        chatMessages.add(message);
        saveChatHistory();
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    // ChatMessage class
    public static class ChatMessage {
        private String text;
        private boolean isUser;
        private String timestamp;

        public ChatMessage(String text, boolean isUser, String timestamp) {
            this.text = text;
            this.isUser = isUser;
            this.timestamp = timestamp;
        }

        // Getters
        public String getText() { return text; }
        public boolean isUser() { return isUser; }
        public String getTimestamp() { return timestamp; }
    }
}