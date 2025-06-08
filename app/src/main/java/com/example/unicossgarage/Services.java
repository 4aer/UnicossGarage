package com.example.unicossgarage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.view.View;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.navigation.NavigationBarView;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Services extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_services);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.services), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView unicossFB = findViewById(R.id.unicossFB);
        TextView unicossIG = findViewById(R.id.unicossIG);

        unicossFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String facebookUrl = "https://www.facebook.com/unicossgarage";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl));
                startActivity(browserIntent);
            }
        });

        unicossIG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String instagramURL = "https://www.instagram.com/unicossgarage";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramURL));
                startActivity(browserIntent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(Services.this, Home.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_update) {
                    Intent intent = new Intent(Services.this, Update.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_summary) {
                    Intent intent = new Intent(Services.this, billing_summary.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_services) {
                    return true;
                } else if (itemId == R.id.nav_logout) {
                    // Show logout confirmation dialog
                    showLogoutConfirmationDialog();
                    return true;
                }
                return false;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_services);
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
                Intent homeIntent = new Intent(Services.this, MainActivity.class);
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
                bottomNavigationView.setSelectedItemId(R.id.nav_services);
            }
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}