package com.example.unicossgarage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        Button submitBtn = findViewById(R.id.submit_btn);
        Button backBtn = findViewById(R.id.back_btn);
        EditText usernameField = findViewById(R.id.register_username);
        EditText emailField = findViewById(R.id.register_email);
        EditText phoneField = findViewById(R.id.register_phone);
        EditText passwordField = findViewById(R.id.register_password);
        EditText confirmPasswordField = findViewById(R.id.confirm_password);
        Spinner securityQuestion = findViewById(R.id.security_question);
        EditText securityAnswer = findViewById(R.id.security_answer);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getText().toString().trim();
                String email = emailField.getText().toString().trim();
                String phone = phoneField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();
                String confirmPassword = confirmPasswordField.getText().toString().trim();
                String answer = securityAnswer.getText().toString().trim();

                // Validate all fields are filled
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                        TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(answer)) {
                    Toast.makeText(Register.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate email format
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Register.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate phone number (basic validation)
                if (phone.length() < 10) {
                    Toast.makeText(Register.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if passwords match
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if security question is selected
                if (securityQuestion.getSelectedItemPosition() == 0) {
                    Toast.makeText(Register.this, "Please select a security question", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences preferences = getSharedPreferences("UserCredentials", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                // Check if username already exists
                if (preferences.contains(username)) {
                    Toast.makeText(Register.this, "Username already exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if email already exists
                if (preferences.contains("email_" + email)) {
                    Toast.makeText(Register.this, "Email already registered", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Store user credentials and additional information
                String selectedQuestion = securityQuestion.getSelectedItem().toString();

                editor.putString(username, password); // username -> password
                editor.putString("email_" + email, username); // email -> username mapping
                editor.putString("user_" + username + "_email", email); // store user's email
                editor.putString("user_" + username + "_phone", phone); // store user's phone
                editor.putString("user_" + username + "_question", selectedQuestion); // store security question
                editor.putString("user_" + username + "_answer", answer.toLowerCase()); // store security answer (lowercase for case-insensitive comparison)
                editor.apply();

                // Set current user session
                SharedPreferences sessionPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                sessionPrefs.edit().putString("current_user", username).apply();

                Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_SHORT).show();

                // Pass username to next activity (if going to home screen or wherever)
                Intent intent = new Intent(Register.this, MainActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.questions_list,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        securityQuestion.setAdapter(adapter);

        securityQuestion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }
                String selectedQuestion = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "Selected: " + selectedQuestion, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
}