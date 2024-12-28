package com.anil.hidecontentcalcy;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {
    private static final String LOG_TAG = "UNHIDE_ACTION";
    private static final String UNHIDDEN_DIR_NAME = "Calcy";

    private final List<FileItem> fileList;

    public FileAdapter(List<FileItem> fileList) {
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return FileViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem fileItem = fileList.get(position);
        holder.bind(fileItem, this::onUnhideClick);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    private void onUnhideClick(FileItem fileItem, int position) {
        String sourceFilePath = fileItem.getFilePath();
        File sourceFile = new File(sourceFilePath);

        if (!sourceFile.exists()) {
            Log.e(LOG_TAG, "Source file does not exist: " + sourceFilePath);
            return;
        }

        File destinationDir = new File(Environment.getExternalStorageDirectory(), UNHIDDEN_DIR_NAME);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        String destinationFilePath = new File(destinationDir, fileItem.getFileName()).getAbsolutePath();
        File destinationFile = new File(destinationFilePath);

        try {
            copyFile(sourceFile, destinationFile);
            deleteSourceFile(sourceFile, position);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error copying file: " + e.getMessage());
        }
    }

    private void copyFile(File sourceFile, File destinationFile) throws IOException {
        try (FileChannel sourceChannel = new FileInputStream(sourceFile).getChannel();
             FileChannel destinationChannel = new FileOutputStream(destinationFile).getChannel()) {
            destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
        Log.d(LOG_TAG, "Copying to unhidden " + destinationFile.getAbsolutePath());
    }

    private boolean deleteSourceFile(File sourceFile, int position) {
        if (sourceFile.delete()) {
            fileList.remove(position);
            notifyItemRemoved(position);
            Log.d(LOG_TAG, "Successfully deleted the source file: " + sourceFile.getAbsolutePath());
            return true;
        } else {
            Log.e(LOG_TAG, "Failed to delete the source file: " + sourceFile.getAbsolutePath());
            return false;
        }
    }
}