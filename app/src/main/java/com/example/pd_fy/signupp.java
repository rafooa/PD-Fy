package com.example.pd_fy;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.storage.StorageMetadata;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import java.io.File;
import java.util.Map;

public class signupp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private static final String TAG = "signupp";
    private EditText emailEditText;
    private EditText name;
    private EditText passwordEditText;
    private EditText confirmp;
    private Button signupButton;

    private Intent myIntent;
    private TextView signInText;
    //private ImageView illustrationImageView;
    private LinearLayout signupContainer;
    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signupp);
        signupContainer = findViewById(R.id.signUpContainer);
        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signupButton = findViewById(R.id.SignupButton);
        signInText = findViewById(R.id.signInText);
        confirmp = findViewById(R.id.confirmPasswordEditText);
        // illustrationImageView = findViewById(R.id.startpic);
        FirebaseApp.initializeApp(this);

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        myIntent = new Intent(this, startpage.class);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // Check if user is signed in (non-null) and update UI accordingly.


            signupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signUp();
                    //startActivity(myIntent);
                }
            });

    }


    private void signUp() {
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String cp = confirmp.getText().toString().trim();
        //final String fullName = name.getText().toString().trim();


        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(cp))  {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(cp))
        {
            confirmp.setError("Passwords do not match.");
            confirmp.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // User is signed up, proceed to save user data in Firestore
                               // saveUserDataToFirestore( email, fullName);
                            }
                        } else {
                          //  Toast.makeText(signupp.this, "Authentication failed.",
                            //        Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        saveUserDataToFirestore(email, "");

    }
    private void saveUserDataToFirestore(String email, String fullName) {
//
//
//
//// Get the reference to the user's folder
//        StorageReference userFolderRef = storageRef.child(email);
//
//// Create metadata to simulate folder creation
//        StorageMetadata metadata = new StorageMetadata.Builder()
//                .setCustomMetadata("isFolder", "true")
//                .build();
//
//// Update metadata to simulate folder creation
//        userFolderRef.updateMetadata(metadata)
//                .addOnSuccessListener(metadataUpdated -> {
//                    // Folder created successfully
//                    Toast.makeText(this, "User folder created successfully", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(exception -> {
//                    // Folder creation failed
//                    Log.e(TAG, "Error creating user folder", exception);
//                    Toast.makeText(this, "Failed to create user folder", Toast.LENGTH_SHORT).show();
//                });


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);

        db.collection("users")
                .document(email) // Set document ID to user's email
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(signupp.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(signupp.this, startpage.class));
                        finish();
                    } else {
                        Log.e(TAG, "Error adding document: ", task.getException());
                        Toast.makeText(signupp.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
