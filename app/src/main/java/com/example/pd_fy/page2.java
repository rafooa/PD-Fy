package com.example.pd_fy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import androidx.appcompat.app.AppCompatActivity;

public class page2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page2);

        Button logoutButton = findViewById(R.id.logoutButton);
        Button convertButton = findViewById(R.id.convertButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(page2.this, startpage.class);
                     startActivity(intent);
                // Handle logout button click
                // You can implement your logout logic here
                // For example, sign out the user and navigate to the login activity
            }
        });

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle convert button click
                Intent intent = new Intent(page2.this, CameraToPdfActivity.class);
                startActivity(intent);
                // You can implement your convert logic here
                // For example, start an activity to convert the images to PDFs
            }
        });
    }
}
