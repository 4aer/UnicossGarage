package com.example.unicossgarage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private static final String DEFAULT_USERNAME = "unicoss";
    private static final String DEFAULT_PASSWORD = "garage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        EditText usernameField = findViewById(R.id.username_input);
        EditText passwordField = findViewById(R.id.password_input);
        ImageView eyeIcon = findViewById(R.id.eyeIcon);
        Button signInButton = findViewById(R.id.signin_btn);
        TextView forgotPassword = findViewById(R.id.forgot_password);
        TextView registerNow = findViewById(R.id.register_now);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });

        eyeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide password
                    passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordField.setSelection(passwordField.length());
                    eyeIcon.setImageResource(R.drawable.hidepass);
                } else {
                    // Show password
                    passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordField.setSelection(passwordField.length());
                    eyeIcon.setImageResource(R.drawable.showpass);
                }
                isPasswordVisible = !isPasswordVisible;
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredUsername = usernameField.getText().toString().trim();
                String enteredPassword = passwordField.getText().toString().trim();

                if (TextUtils.isEmpty(enteredUsername) || TextUtils.isEmpty(enteredPassword)) {
                    Toast.makeText(MainActivity.this, "Fill out the empty fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check default credentials first
                if (enteredUsername.equals(DEFAULT_USERNAME) && enteredPassword.equals(DEFAULT_PASSWORD)) {
                    // Set current user session for default user
                    setCurrentUserSession(DEFAULT_USERNAME);

                    Intent intent = new Intent(MainActivity.this, Home.class);
                    intent.putExtra("username", DEFAULT_USERNAME);
                    startActivity(intent);
                    finish();
                    return;
                }

                // Check registered user credentials
                SharedPreferences preferences = getSharedPreferences("UserCredentials", MODE_PRIVATE);
                String storedPassword = preferences.getString(enteredUsername, null);

                if (storedPassword != null && storedPassword.equals(enteredPassword)) {
                    // Set current user session for registered user
                    setCurrentUserSession(enteredUsername);

                    Intent intent = new Intent(MainActivity.this, Home.class);
                    intent.putExtra("username", enteredUsername);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });
    }

    private void setCurrentUserSession(String username) {
        SharedPreferences sessionPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        sessionPrefs.edit().putString("current_user", username).apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // clear any existing session when returning to login page
        // users must log in again if they return to this activity
        clearUserSession();
    }

    private void clearUserSession() {
        SharedPreferences sessionPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        sessionPrefs.edit().clear().apply();
    }
}