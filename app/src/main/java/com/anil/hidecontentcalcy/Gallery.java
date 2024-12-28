package com.anil.hidecontentcalcy;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Gallery extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        ListView audioListView = findViewById(R.id.audioListView);
        ListView documentListView = findViewById(R.id.documentListView);
        GridView mediaGridView = findViewById(R.id.mediaGridView);

        if (getIntent().hasExtra("run_for_audio")) {
            fetchAudioFiles();
            audioListView.setVisibility(View.VISIBLE);
            documentListView.setVisibility(View.GONE);
            mediaGridView.setVisibility(View.GONE);
        } else if (getIntent().hasExtra("run_for_documents")) {
            fetchDocumentFiles();
            audioListView.setVisibility(View.GONE);
            documentListView.setVisibility(View.VISIBLE);
            mediaGridView.setVisibility(View.GONE);
        } else if (getIntent().hasExtra("run_for_images")) {
            fetchImageFiles(mediaGridView);
            audioListView.setVisibility(View.GONE);
            documentListView.setVisibility(View.GONE);
            mediaGridView.setVisibility(View.VISIBLE);
        } else if (getIntent().hasExtra("run_for_videos")) {
            fetchVideoFiles(mediaGridView);
            audioListView.setVisibility(View.GONE);
            documentListView.setVisibility(View.GONE);
            mediaGridView.setVisibility(View.VISIBLE);
        }
    }

    private void fetchImageFiles(GridView mediaGridView) {
        ArrayList<String> mediaPaths = new ArrayList<>();
        ArrayList<Boolean> isVideoList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        Cursor cursor = contentResolver.query(imageUri, null, null, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            do {
                String filePath = cursor.getString(dataColumn);
                mediaPaths.add(filePath);
                isVideoList.add(false); // All items are images
            } while (cursor.moveToNext());

            cursor.close();
        }

        ImageAndVideoAdapter mediaAdapter = new ImageAndVideoAdapter(this, mediaPaths, isVideoList);
        mediaGridView.setAdapter(mediaAdapter);
    }

    private void fetchVideoFiles(GridView mediaGridView) {
        ArrayList<String> mediaPaths = new ArrayList<>();
        ArrayList<Boolean> isVideoList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC";
        Cursor cursor = contentResolver.query(videoUri, null, null, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);

            do {
                String filePath = cursor.getString(dataColumn);
                mediaPaths.add(filePath);
                isVideoList.add(true);
            } while (cursor.moveToNext());

            cursor.close();
        }

        ImageAndVideoAdapter mediaAdapter = new ImageAndVideoAdapter(this, mediaPaths, isVideoList);
        mediaGridView.setAdapter(mediaAdapter);

    }

    private void fetchAudioFiles() {
        ArrayList<String> audioList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
        Cursor cursor = contentResolver.query(audioUri, null, null, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int dateModifiedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);

            do {
                String title = cursor.getString(titleColumn);
                String filePath = cursor.getString(dataColumn);
                long dateModified = cursor.getLong(dateModifiedColumn);

                audioList.add(title);
            } while (cursor.moveToNext());

            cursor.close();
        }

        ListView audioListView = findViewById(R.id.audioListView);
        Adapters adapter = new Adapters(this, audioList, "audio");
        audioListView.setAdapter(adapter);
    }


    private void fetchDocumentFiles() {

        Log.d("FfetchDocumentFiles", "fetchDocumentFiles is getting called");
        ArrayList<String> documentList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();

        Uri documentUri = MediaStore.Files.getContentUri("external");

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " IN (?, ?, ?, ?, ?, ?)";

        String[] selectionArgs = new String[] {
                "application/pdf",
                "application/msword",
                "application/vnd.ms-excel",
                "text/plain",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        };

        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";

        Cursor cursor = contentResolver.query(documentUri, null, selection, selectionArgs, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            int titleColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
            int dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            int dateModifiedColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);

            do {
                String title = cursor.getString(titleColumn);
                String filePath = cursor.getString(dataColumn);
                long dateModified = cursor.getLong(dateModifiedColumn);

                documentList.add(title);
            } while (cursor.moveToNext());

            cursor.close();
        }

        ListView documentListView = findViewById(R.id.documentListView);
        Adapters adapter = new Adapters(this, documentList, "document");
        documentListView.setAdapter(adapter);
    }



}

