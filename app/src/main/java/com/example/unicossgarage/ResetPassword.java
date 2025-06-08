package com.example.unicossgarage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPassword extends AppCompatActivity {

    private EditText newPasswordField;
    private EditText confirmPasswordField;
    private Button resetBtn;
    private Button backBtn;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        // Get username from intent
        username = getIntent().getStringExtra("username");

        if (username == null) {
            Toast.makeText(this, "Error: Username not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        newPasswordField = findViewById(R.id.new_password_input);
        confirmPasswordField = findViewById(R.id.confirm_new_password_input);
        resetBtn = findViewById(R.id.reset_password_btn);
        backBtn = findViewById(R.id.back_btn);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResetPassword.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void resetPassword() {
        String newPassword = newPasswordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update password in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserCredentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(username, newPassword);
        editor.apply();

        Toast.makeText(this, "Password reset successful! Please login with your new password.", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(ResetPassword.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}