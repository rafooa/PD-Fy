package com.example.pd_fy;

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
import java.util.Date;

public class CameraToPdfActivity extends AppCompatActivity {
    private static final String TAG = CameraToPdfActivity.class.getSimpleName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private String currentPhotoPath;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 123;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_picture);
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
            dispatchTakePictureIntent();
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
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
        else
            Log.d(TAG, "No Camera App");
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
}