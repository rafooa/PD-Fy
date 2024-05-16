package com.example.pd_fy;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraToPdfActivity extends AppCompatActivity {
    private static final String TAG = CameraToPdfActivity.class.getSimpleName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private String currentPhotoPath;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 123;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    FirebaseAuth mAuth;


    // Initialize FirebaseStorage instance
    FirebaseStorage storage ;

    // Get a reference to the root of the Firebase Storage
    StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_picture);

         mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();


        // Register the permissions callback
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission granted, proceed with camera functionality
                        dispatchTakePictureIntent();
                    } else {
                        // Permission denied
                        Toast.makeText(CameraToPdfActivity.this, "Camera permission is required to capture images.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Check if the CAMERA permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed with camera functionality
            //dispatchTakePictureIntent();
        } else {
            // Request the CAMERA permission
            requestCameraPermission();
        }

        imageView = findViewById(R.id.imageView);
        Button captureButton = findViewById(R.id.captureButton);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Capture Button Clicked");
                dispatchTakePictureIntent();
            }
        });
    }
    private void requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }
    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.d(TAG, "Func entered");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
               // Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.pd_fy.provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
            Button editButton = findViewById(R.id.editButton);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToImageProcessing();
                }
            });
        }
        else
            Log.d(TAG, "No Camera App");
    }
    private void navigateToImageProcessing() {
        Intent intent = new Intent(this, ImageProcessing.class);
        startActivity(intent);
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            imageView.setImageBitmap(bitmap);
            convertToPdf(bitmap);
        }
    }

    private void convertToPdf(Bitmap bitmap) {
        File pdfFile = generatePdfFile(bitmap);
        if (pdfFile != null) {

            uploadPdfToStorage(pdfFile);
            Toast.makeText(this, "PDF created successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to create PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private File generatePdfFile(Bitmap bitmap) {
        File pdfFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String pdfFileName = "IMAGE_" + timeStamp + ".pdf";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            pdfFile = new File(storageDir, pdfFileName);

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            Image image = Image.getInstance(byteArray);
            image.scaleToFit(PageSize.A4.getWidth() - 72, PageSize.A4.getHeight() - 72);
            image.setAlignment(Image.ALIGN_CENTER);

            document.add(image);
            document.close();
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            return null;
        }
        return pdfFile;
    }
//    private void uploadPdfToStorage(File pdfFile) {
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            // Get the current user's email
//            String userEmail = currentUser.getEmail();
//
//            // Create a reference to the folder with the user's email as its name
//            StorageReference userFolderRef = storageRef.child(userEmail);
//
//            // Upload the PDF file to the user's folder in Firebase Storage
//            StorageReference pdfRef = userFolderRef.child(pdfFile.getName());
//
//            // Upload the file
//            pdfRef.putFile(Uri.fromFile(pdfFile))
//                    .addOnSuccessListener(taskSnapshot -> {
//                        Toast.makeText(CameraToPdfActivity.this, "PDF uploaded successfully", Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(CameraToPdfActivity.this, "Failed to upload PDF", Toast.LENGTH_SHORT).show();
//                    });
//        }
//    }

    // Create a method to upload the PDF file to Firebase Storage
    private void uploadPdfToStorage(File pdfFile) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to the PDF file in Firebase Storage
        StorageReference pdfRef = storageRef.child(pdfFile.getName());

        // Upload the PDF file to Firebase Storage
        pdfRef.putFile(Uri.fromFile(pdfFile))
                .addOnSuccessListener(taskSnapshot -> {
                    // PDF uploaded successfully
                    Toast.makeText(CameraToPdfActivity.this, "PDF uploaded successfully", Toast.LENGTH_SHORT).show();

                    // Get the download URL of the uploaded PDF file
                    pdfRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // URL retrieved successfully
                        String pdfUrl = uri.toString();

                        // Store metadata and PDF URL in Firestore
                        storePdfMetadataInFirestore(pdfFile.getName(), pdfUrl);
                    }).addOnFailureListener(e -> {
                        // Failed to retrieve the URL
                        Toast.makeText(CameraToPdfActivity.this, "Failed to get PDF URL", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    // Failed to upload PDF
                    Toast.makeText(CameraToPdfActivity.this, "Failed to upload PDF", Toast.LENGTH_SHORT).show();
                });
    }

//    private void storePdfMetadataInFirestore(String pdfFileName, String pdfUrl) {
//        // Get the current user's email or any unique identifier
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser != null) {
//            String userEmail = currentUser.getEmail();
//
//            // Create a reference to the Firestore collection "pdf_metadata"
//            FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//            // Create a new document with the PDF metadata
//            Map<String, Object> metadata = new HashMap<>();
//            metadata.put("fileName", pdfFileName);
//            metadata.put("downloadUrl", pdfUrl);
//
//            // Store the metadata in Firestore
//            db.collection("users")
//                    .document(userEmail)
//                    .set(metadata)
//                    .addOnSuccessListener(aVoid -> {
//                        // Metadata stored in Firestore successfully
//                        Toast.makeText(CameraToPdfActivity.this, "PDF metadata stored in Firestore", Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnFailureListener(e -> {
//                        // Failed to store metadata in Firestore
//                        Toast.makeText(CameraToPdfActivity.this, "Failed to store PDF metadata in Firestore", Toast.LENGTH_SHORT).show();
//                    });
//        }
//    }

    private void storePdfMetadataInFirestore(String pdfFileName, String pdfUrl) {
        // Get the current user's email or any unique identifier
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            // Create a reference to the Firestore collection "pdf_metadata"
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Create a new document with the PDF metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fileName", pdfFileName);
            metadata.put("downloadUrl", pdfUrl);

            // Check if the user document exists in Firestore
            DocumentReference userDocRef = db.collection("users").document(userEmail);
            userDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // User document exists, update the arrays if they exist
                        List<String> fileNames = (List<String>) document.get("fileNames");
                        List<String> downloadUrls = (List<String>) document.get("downloadUrls");

                        // Add the new metadata to the arrays
                        if (fileNames == null) {
                            fileNames = new ArrayList<>();
                        }
                        if (downloadUrls == null) {
                            downloadUrls = new ArrayList<>();
                        }

                        // Add the filename and download URL to the arrays
                        fileNames.add(pdfFileName);
                        downloadUrls.add(pdfUrl);

                        // Update the document with the new arrays
                        userDocRef.update("fileNames", fileNames, "downloadUrls", downloadUrls)
                                .addOnSuccessListener(aVoid -> {
                                    // Metadata stored in Firestore successfully
                                    Toast.makeText(CameraToPdfActivity.this, "PDF metadata stored in Firestore", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Failed to store metadata in Firestore
                                    Toast.makeText(CameraToPdfActivity.this, "Failed to store PDF metadata in Firestore", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // User document does not exist, create a new document with arrays
                        List<String> fileNames = new ArrayList<>();
                        List<String> downloadUrls = new ArrayList<>();

                        // Add the filename and download URL to the arrays
                        fileNames.add(pdfFileName);
                        downloadUrls.add(pdfUrl);

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("fileNames", fileNames);
                        userData.put("downloadUrls", downloadUrls);

                        // Create the document with the arrays
                        db.collection("users").document(userEmail)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    // Metadata stored in Firestore successfully
                                    Toast.makeText(CameraToPdfActivity.this, "PDF metadata stored in Firestore", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Failed to store metadata in Firestore
                                    Toast.makeText(CameraToPdfActivity.this, "Failed to store PDF metadata in Firestore", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    // Error getting document
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(CameraToPdfActivity.this, "Error getting document", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}
