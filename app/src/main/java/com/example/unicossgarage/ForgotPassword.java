package com.example.unicossgarage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailField;
    private EditText answerField;
    private TextView questionText;
    private Button verifyEmailBtn;
    private Button submitAnswerBtn;
    private Button backBtn;
    private LinearLayout questionSection;

    private String currentUsername;
    private String currentEmail;
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 30 * 60 * 1000; // 30 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        emailField = findViewById(R.id.email_input);
        answerField = findViewById(R.id.answer_input);
        questionText = findViewById(R.id.security_question_text);
        verifyEmailBtn = findViewById(R.id.verify_email_btn);
        submitAnswerBtn = findViewById(R.id.submit_answer_btn);
        backBtn = findViewById(R.id.back_btn);
        questionSection = findViewById(R.id.question_section);

        questionSection.setVisibility(View.GONE);

        verifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyEmail();
            }
        });

        submitAnswerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSecurityAnswer();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPassword.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void verifyEmail() {
        String email = emailField.getText().toString().trim().toLowerCase();
        currentEmail = email;

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences("UserCredentials", MODE_PRIVATE);
        SharedPreferences attemptPrefs = getSharedPreferences("ForgotPasswordPrefs", MODE_PRIVATE);

        long currentTime = System.currentTimeMillis();
        long lockoutEnd = attemptPrefs.getLong("lockout_" + email, 0);

        if (currentTime < lockoutEnd) {
            long remainingMinutes = (lockoutEnd - currentTime) / 60000;
            Toast.makeText(this, "Too many attempts. Try again in " + remainingMinutes + " minute(s).", Toast.LENGTH_LONG).show();
            return;
        }

        currentUsername = preferences.getString("email_" + email, null);

        if (currentUsername == null) {
            Toast.makeText(this, "Email not found in our records", Toast.LENGTH_SHORT).show();
            return;
        }

        String securityQuestion = preferences.getString("user_" + currentUsername + "_question", null);

        if (securityQuestion == null) {
            Toast.makeText(this, "Security question not found for this account", Toast.LENGTH_SHORT).show();
            return;
        }

        questionText.setText(securityQuestion);
        questionSection.setVisibility(View.VISIBLE);
        emailField.setEnabled(false);
        verifyEmailBtn.setEnabled(false);

        Toast.makeText(this, "Email verified! Please answer the security question.", Toast.LENGTH_SHORT).show();
    }

    private void checkSecurityAnswer() {
        String userAnswer = answerField.getText().toString().trim();

        if (TextUtils.isEmpty(userAnswer)) {
            Toast.makeText(this, "Please enter your answer", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences("UserCredentials", MODE_PRIVATE);
        SharedPreferences attemptPrefs = getSharedPreferences("ForgotPasswordPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = attemptPrefs.edit();

        String correctAnswer = preferences.getString("user_" + currentUsername + "_answer", null);
        String email = currentEmail;

        int attempts = attemptPrefs.getInt("attempts_" + email, 0);

        if (correctAnswer != null && correctAnswer.equals(userAnswer.toLowerCase())) {
            // Reset attempt counter on success
            editor.putInt("attempts_" + email, 0);
            editor.remove("lockout_" + email);
            editor.apply();
            showPasswordResetDialog();
        } else {
            attempts++;
            if (attempts >= MAX_ATTEMPTS) {
                long lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
                editor.putLong("lockout_" + email, lockoutUntil);
                editor.putInt("attempts_" + email, 0); // Reset after lock
                editor.apply();
                Toast.makeText(this, "Too many failed attempts. You are locked out for 30 minutes.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ForgotPassword.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                editor.putInt("attempts_" + email, attempts);
                editor.apply();
                int remaining = MAX_ATTEMPTS - attempts;
                Toast.makeText(this, "Incorrect answer. " + remaining + " attempt(s) remaining.", Toast.LENGTH_SHORT).show();
                answerField.setText("");
            }
        }
    }

    private void showPasswordResetDialog() {
        Intent intent = new Intent(ForgotPassword.this, ResetPassword.class);
        intent.putExtra("username", currentUsername);
        startActivity(intent);
        finish();
    }
}
