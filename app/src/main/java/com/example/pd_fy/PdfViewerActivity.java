package com.example.pd_fy;


import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pd_fy.R;

public class PdfViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        // Get the PDF URL from the intent
        String pdfUrl = getIntent().getStringExtra("pdfUrl");

        // Find the WebView in your layout
        WebView webView = findViewById(R.id.webView);

        // Enable JavaScript in the WebView (optional)
        webView.getSettings().setJavaScriptEnabled(true);

        // Load the PDF URL in the WebView using Google Docs Viewer
        webView.loadUrl("https://docs.google.com/viewer?url=" + pdfUrl);
    }
}
