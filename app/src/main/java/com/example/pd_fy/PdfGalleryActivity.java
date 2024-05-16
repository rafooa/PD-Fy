package com.example.pd_fy;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PdfGalleryActivity extends AppCompatActivity{
    private List<PdfItem> pdfList;
    private PdfGalleryAdapter pdfAdapter;
    private ImageButton logoutButton;
    private Button addNewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_gallery);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        pdfList = new ArrayList<>();
        pdfAdapter = new PdfGalleryAdapter(pdfList, this, this);
        recyclerView.setAdapter(pdfAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        logoutButton = findViewById(R.id.logoutButton);
        addNewButton = findViewById(R.id.addNewButton);

        logoutButton.setOnClickListener(v -> logout());
        addNewButton.setOnClickListener(v -> openCameraToPdfActivity());

        // Call method to fetch PDF documents from Firestore and update the adapter
        fetchPdfDocumentsFromFirestore();
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> confirmLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmLogout()
    {
        Intent intent = new Intent(this, startpage.class);
        FirebaseAuth.getInstance().signOut();
        startActivity(intent);
        finish(); // Optional: Finish the current activity to prevent going back to it
    }
    private void openCameraToPdfActivity() {
        Intent intent = new Intent(this, CameraToPdfActivity.class);
        startActivity(intent);
    }

    void tryDelete(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this PDF?")
                .setPositiveButton("Delete", (dialog, which) -> deleteItem(position))
                .setNegativeButton("Cancel", null)
                .show();
    }
    void renameFileInFirestore(int position, String newName) {
        PdfItem pdfItem = pdfList.get(position);
        String oldName = pdfItem.getName();
        String downloadUrl = pdfItem.getUrl();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userDocumentRef = db.collection("users").document(userEmail);

            userDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> fileNames = (List<String>) documentSnapshot.get("fileNames");
                    List<String> downloadUrls = (List<String>) documentSnapshot.get("downloadUrls");

                    if (fileNames != null && downloadUrls != null && fileNames.size() == downloadUrls.size()) {
                        int indexToRename = fileNames.indexOf(oldName);
                        if (indexToRename != -1) {
                            fileNames.set(indexToRename, newName);
                            Log.d(TAG, "File renamed in firestore");
                            // Update the document with the modified arrays
                            userDocumentRef.update("fileNames", fileNames);
                        }
                    }
                }
            });
        }
    }
    void deleteItem(int position) {
        PdfItem selectedItem = pdfList.get(position);

        // Remove the item from the local list
        pdfList.remove(position);
        pdfAdapter.notifyItemRemoved(position);

        // Remove the item from Firestore
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userDocumentRef = db.collection("users").document(userEmail);

            userDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> fileNames = (List<String>) documentSnapshot.get("fileNames");
                    List<String> downloadUrls = (List<String>) documentSnapshot.get("downloadUrls");

                    if (fileNames != null && downloadUrls != null && fileNames.size() == downloadUrls.size()) {
                        int indexToRemove = fileNames.indexOf(selectedItem.getName());
                        if (indexToRemove != -1) {
                            fileNames.remove(indexToRemove);
                            downloadUrls.remove(indexToRemove);

                            // Update the document with the modified arrays
                            userDocumentRef.update("fileNames", fileNames, "downloadUrls", downloadUrls);
                        }
                    }
                }
            });
        }
    }

    private void fetchPdfDocumentsFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Get the reference to the Firestore document containing the PDF arrays
            DocumentReference userDocumentRef = db.collection("users").document(userEmail);

            userDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Extract arrays from the document snapshot
                    List<String> fileNames = (List<String>) documentSnapshot.get("fileNames");
                    List<String> downloadUrls = (List<String>) documentSnapshot.get("downloadUrls");

                    // Clear the existing list
                    pdfList.clear();

                    // Check if both arrays are not null and have the same size
                    if (fileNames != null && downloadUrls != null && fileNames.size() == downloadUrls.size()) {
                        // Iterate over the arrays and add PDF items to the list
                        for (int i = 0; i < fileNames.size(); i++) {
                            String pdfName = fileNames.get(i);
                            String downloadUrl = downloadUrls.get(i);
                            pdfList.add(new PdfItem(pdfName, downloadUrl));
                        }
                        pdfAdapter.notifyDataSetChanged(); // Notify adapter that dataset has changed
                    } else {
                        // Handle null or mismatched arrays
                        Log.e(TAG, "File names and download URLs arrays are null or have different sizes");
                        Toast.makeText(PdfGalleryActivity.this, "Failed to fetch PDF documents", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle non-existent document
                    Log.e(TAG, "User document does not exist");
                    Toast.makeText(PdfGalleryActivity.this, "User document does not exist", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                // Handle failure to fetch document from Firestore
                Log.e(TAG, "Error fetching user document from Firestore", e);
                Toast.makeText(PdfGalleryActivity.this, "Failed to fetch user document", Toast.LENGTH_SHORT).show();
            });
        }
    }

}

