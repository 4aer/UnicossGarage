package com.example.unicossgarage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.navigation.NavigationBarView;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity {

    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get current user
        getCurrentUser();

        Button seeMoreBtn = findViewById(R.id.seeMoreBtn);
        ImageView chatBotBtn = findViewById(R.id.chatBotBtn);

        seeMoreBtn.setOnClickListener(new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Services.class);
                // Pass username to maintain session
                if (currentUsername != null) {
                    intent.putExtra("username", currentUsername);
                }
                startActivity(intent);
            }
        });

        chatBotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, ChatBot.class);
                // Pass username to ChatBot for user-specific chat history
                if (currentUsername != null) {
                    intent.putExtra("username", currentUsername);
                }
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_update) {
                    Intent intent = new Intent(Home.this, Update.class);
                    if (currentUsername != null) {
                        intent.putExtra("username", currentUsername);
                    }
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_summary) {
                    Intent intent = new Intent(Home.this, billing_summary.class);
                    // Pass username to maintain session
                    if (currentUsername != null) {
                        intent.putExtra("username", currentUsername);
                    }
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_services) {
                    Intent intent = new Intent(Home.this, Services.class);
                    // Pass username to maintain session
                    if (currentUsername != null) {
                        intent.putExtra("username", currentUsername);
                    }
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_logout) {
                    // Show logout confirmation dialog
                    showLogoutConfirmationDialog();
                    return true;
                }
                return false;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void getCurrentUser() {
        // First try to get from Intent (if passed from another activity)
        currentUsername = getIntent().getStringExtra("username");

        // If not found in Intent, try to get from SharedPreferences (current session)
        if (currentUsername == null) {
            SharedPreferences sessionPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            currentUsername = sessionPrefs.getString("current_user", null);
        }

        // If we have a username, make sure it's stored in session
        if (currentUsername != null) {
            SharedPreferences sessionPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            sessionPrefs.edit().putString("current_user", currentUsername).apply();
        }
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Do you want to log out?");

        // Add the Yes button
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Clear user session - THIS IS THE KEY CHANGE
                clearUserSession();

                // User confirmed logout
                Intent homeIntent = new Intent(Home.this, MainActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                finish();
            }
        });

        // Add the No button
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User canceled logout
                dialog.dismiss();

                // Reset the selected navigation item to home
                BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void clearUserSession() {
        // Clear current user session
        SharedPreferences sessionPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        sessionPrefs.edit().clear().apply();

        // Note: Comment niyo kung gusto niyo nasasave yung chat history per account sa chatbot
        if (currentUsername != null) {
            SharedPreferences chatPrefs = getSharedPreferences("ChatBotPrefs", MODE_PRIVATE);
            chatPrefs.edit().remove("chat_messages_" + currentUsername).apply();
        }
    }
}