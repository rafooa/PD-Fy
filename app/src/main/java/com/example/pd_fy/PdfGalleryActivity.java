package com.example.pd_fy;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
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

public class PdfGalleryActivity extends AppCompatActivity {
    private List<PdfItem> pdfList;
    private PdfGalleryAdapter pdfAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_gallery);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        pdfList = new ArrayList<>();
        pdfAdapter = new PdfGalleryAdapter(pdfList);
        recyclerView.setAdapter(pdfAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Call method to fetch PDF documents from Firestore and update the adapter
        fetchPdfDocumentsFromFirestore();
    }

    private void fetchPdfDocumentsFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Get the reference to the Firestore document containing the PDF arrays
            DocumentReference userDocumentRef = db.collection("users").document("a1@gmail.com");

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

