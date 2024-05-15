package com.example.pd_fy;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PdfGalleryAdapter extends RecyclerView.Adapter<PdfGalleryAdapter.ViewHolder> {

    private List<PdfItem> pdfItems;
    private Context context;

    public PdfGalleryAdapter(List<PdfItem> pdfItems) {
        this.context = context;
        this.pdfItems = pdfItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_gallery_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PdfItem pdfItem = pdfItems.get(position);
        holder.pdfNameTextView.setText(pdfItem.getName());

        holder.itemView.setOnClickListener(v -> {

            String url = pdfItem.getUrl();
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // PDF viewer app is not installed, handle the exception
                    Toast.makeText(context, "No PDF viewer app installed", Toast.LENGTH_SHORT).show();
                }
            }

        });



    }

    @Override
    public int getItemCount() {
        return pdfItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView pdfNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pdfNameTextView = itemView.findViewById(R.id.pdfNameTextView);
        }
    }
}
