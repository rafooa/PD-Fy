package com.example.pd_fy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
public class loginp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "loginp";
    private EditText rollNumberEditText;
    private EditText passwordEditText;
    private Button logInButton;
    private Button backButton;
    private TextView signInText;
    //private ImageView illustrationImageView;
    private LinearLayout signInContainer;
    private RelativeLayout fullscreen;
    //@SuppressLint("WrongViewCast")
    //@SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginp);
        fullscreen = findViewById(R.id.fullscreen);
        signInContainer = findViewById(R.id.signInContainer);
        // Initialize views
        rollNumberEditText = findViewById(R.id.rollNumberEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        logInButton = findViewById(R.id.logInButton);
        backButton = findViewById(R.id.backButton);
        signInText = findViewById(R.id.signInText);
       // illustrationImageView = findViewById(R.id.startpic);


        mAuth = FirebaseAuth.getInstance();




        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            // User is signed in, proceed to the main activity
//            startActivity(new Intent(loginp.this, CameraToPdfActivity.class));
//            //finish(); // Prevent going back to the login activity
//        }
//        else
//        {
            logInButton.setOnClickListener(new View.OnClickListener() {
                @Override

                public void onClick(View v) {
                    // Your login authentication logic goes here
                    signIn();
                    // If login is successful, navigate to the second activity
                  //  startActivity(new Intent(loginp.this, PdfGalleryActivity.class));
                   //finish();
                }
            });
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(loginp.this, startpage.class);
                    startActivity(intent);
                }
            });

        //}
        // Your Java code for handling login functionality goes here
    }


    private void signIn() {


        String email = rollNumberEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            rollNumberEditText.setError("Email is required");
            rollNumberEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            rollNumberEditText.setError("Please enter a valid email");
            rollNumberEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Proceed to the main activity
                        startActivity(new Intent(loginp.this, PdfGalleryActivity.class));
                        finish(); // Prevent going back to the login activity
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(loginp.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
