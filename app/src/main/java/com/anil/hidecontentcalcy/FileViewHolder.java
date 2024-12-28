package com.anil.hidecontentcalcy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FileViewHolder extends RecyclerView.ViewHolder {
    private final TextView fileNameTextView;
    private final TextView createdTimeTextView;
    private final TextView fileSizeTextView;
    private final ImageView unhideIcon;

    public FileViewHolder(@NonNull View itemView) {
        super(itemView);
        fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
        createdTimeTextView = itemView.findViewById(R.id.createdTimeTextView);
        fileSizeTextView = itemView.findViewById(R.id.fileSizeTextView);
        unhideIcon = itemView.findViewById(R.id.unhideIcon);
    }

    public static FileViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
        return new FileViewHolder(view);
    }

    public void bind(FileItem fileItem, OnUnhideClickListener listener) {
        fileNameTextView.setText(fileItem.getFileName());
        createdTimeTextView.setText(fileItem.getCreatedTime());
        fileSizeTextView.setText(fileItem.getFileSize());

        unhideIcon.setOnClickListener(v -> listener.onUnhideClick(fileItem, getAdapterPosition()));
    }

    public interface OnUnhideClickListener {
        void onUnhideClick(FileItem fileItem, int position);
    }
}