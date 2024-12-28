package com.anil.hidecontentcalcy;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Vault extends AppCompatActivity {

    private ActivityResultLauncher<Intent> pickMediaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedMediaUri = result.getData().getData();
                    if (selectedMediaUri != null) {
                        copyFileToHiddenDirectory(selectedMediaUri);
                    }
                }
            }
    );

    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<FileItem> fileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        recyclerView = findViewById(R.id.gridRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(fileList);
        recyclerView.setAdapter(fileAdapter);

        FloatingActionButton btnAddMedia = findViewById(R.id.btnAddMedia);
        btnAddMedia.setOnClickListener(v -> openFileManager());

        Uri fileUri = getIntent().getParcelableExtra("FILE_URI");
        if (fileUri != null) {
            copyFileToHiddenDirectory(fileUri);
        }

        String category = getIntent().getStringExtra("category");

        loadHiddenFiles(category);

    }

    private void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        pickMediaLauncher.launch(intent);
    }


    public void copyFileToHiddenDirectory(Uri sourceUri) {
        try {
            Log.d("sourceUri", "SOURCE "+ sourceUri);
            File hiddenDir = new File(getExternalFilesDir(null), ".calcy_Safe");
            if (!hiddenDir.exists()) {
                hiddenDir.mkdirs();
            }

            String originalFileName = getFileName(sourceUri);
            if (originalFileName == null) {
                originalFileName = "unknown";
            }

            int maxFileNameLength = 100;

            String nameWithoutExtension = originalFileName;
            int lastDotIndex = originalFileName.lastIndexOf(".");
            if (lastDotIndex > 0) {
                nameWithoutExtension = originalFileName.substring(0, lastDotIndex);
            }

            if (nameWithoutExtension.length() > maxFileNameLength) {
                nameWithoutExtension = nameWithoutExtension.substring(0, maxFileNameLength);
            }

            String sourceExtension = null;
            String mimeType = getContentResolver().getType(sourceUri);
            if (mimeType != null) {
                sourceExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            }

            if (sourceExtension == null && lastDotIndex > 0) {
                sourceExtension = originalFileName.substring(lastDotIndex + 1);
            }

            String destinationFileName = nameWithoutExtension + "_" + System.currentTimeMillis() +
                    (sourceExtension != null ? "." + sourceExtension : "");

            if (destinationFileName.length() > maxFileNameLength) {
                destinationFileName = destinationFileName.substring(0, maxFileNameLength);
            }

            File hiddenFile = new File(hiddenDir, destinationFileName);

            copyFile(sourceUri, hiddenFile);

            deleteSourceFile(sourceUri);

            String category = getIntent().getStringExtra("category");
            loadHiddenFiles(category);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e("FILE_NAME", "Error getting filename: " + e.getMessage());
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    private void copyFile(Uri sourceUri, File destFile) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(destFile)) {

            if (inputStream == null) {
                throw new IOException("Failed to open input stream");
            }

            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    private void deleteSourceFile(Uri sourceUri) {
        try {
            // Method 1: Using DocumentFile
            DocumentFile documentFile = DocumentFile.fromSingleUri(this, sourceUri);
            if (documentFile != null && documentFile.exists()) {
                boolean deleted = documentFile.delete();
                if (deleted) {
                    Log.d("UNHIDE_ACTION", "Source file deleted successfully");
                } else {
                    Log.w("UNHIDE_ACTION", "Failed to delete source file using DocumentFile");
                    deleteUsingContentResolver(sourceUri);
                }
            }
        } catch (Exception e) {
            Log.e("UNHIDE_ACTION", "Error deleting file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteUsingContentResolver(Uri sourceUri) {
        try {
            // Method 2: Using ContentResolver
            int rowsDeleted = getContentResolver().delete(sourceUri, null, null);

            if (rowsDeleted > 0) {
                Log.d("UNHIDE_ACTION", "Source file deleted using ContentResolver");
            } else {
                Log.w("UNHIDE_ACTION", "Failed to delete source file using ContentResolver");
            }
        } catch (SecurityException e) {
            Log.e("UNHIDE_ACTION", "Security Exception while deleting: " + e.getMessage());
        }
    }

    private void loadHiddenFiles(String category) {
        fileList.clear();
        File hiddenDir = new File(getExternalFilesDir(null), ".calcy_Safe");
        if (hiddenDir.exists() && hiddenDir.isDirectory()) {
            File[] files = hiddenDir.listFiles();
            if (files != null) {
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {
                        long file1ModifiedTime = file1.lastModified();
                        long file2ModifiedTime = file2.lastModified();
                        return Long.compare(file2ModifiedTime, file1ModifiedTime);
                    }
                });

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                for (File file : files) {
                    if (category.equals("PHOTO_AND_VIDEO") && isPhotoOrVideo(file)) {
                        addFileToList(file, dateFormat);
                    } else if (category.equals("AUDIO") && isAudio(file)) {
                        addFileToList(file, dateFormat);
                    } else if (category.equals("DOCUMENT") && isDocument(file)) {
                        addFileToList(file, dateFormat);
                    }
                }
            }
        }
        fileAdapter.notifyDataSetChanged();
    }


    private void addFileToList(File file, SimpleDateFormat dateFormat) {
        String fileName = file.getName();
        String createdTime = dateFormat.format(new Date(file.lastModified()));
        String fileSize = formatFileSize(file.length());
        String filePath = file.getPath();
        fileList.add(new FileItem(fileName, createdTime, fileSize, filePath));
    }

    private boolean isPhotoOrVideo(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                fileName.endsWith(".mp4") || fileName.endsWith(".avi") ||
                fileName.endsWith(".mkv") || fileName.endsWith(".mov");
    }

    private boolean isAudio(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".mp3") || fileName.endsWith(".wav") ||
                fileName.endsWith(".flac") || fileName.endsWith(".aac");
    }

    private boolean isDocument(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".pdf") || fileName.endsWith(".docx") ||
                fileName.endsWith(".txt") || fileName.endsWith(".xlsx") ||
                fileName.endsWith(".pptx");
    }



    private String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return (sizeInBytes / 1024) + " KB";
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return (sizeInBytes / (1024 * 1024)) + " MB";
        } else {
            return (sizeInBytes / (1024 * 1024 * 1024)) + " GB";
        }
    }

}
