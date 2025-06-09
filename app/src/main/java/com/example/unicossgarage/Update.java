package com.example.unicossgarage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.navigation.NavigationBarView;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Update extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.update), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView chatBotBtn = findViewById(R.id.chatBotBtn);

        chatBotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Update.this, ChatBot.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(Update.this, Home.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_update) {
                    return true;
                } else if (itemId == R.id.nav_summary) {
                    Intent intent = new Intent(Update.this, billing_summary.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_services) {
                    Intent intent = new Intent(Update.this, Services.class);
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

        bottomNavigationView.setSelectedItemId(R.id.nav_update);
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Do you want to log out?");

        // Add the Yes button
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User confirmed logout
                Intent homeIntent = new Intent(Update.this, MainActivity.class);
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

                // Reset the selected navigation item
                BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setSelectedItemId(R.id.nav_update);
            }
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}