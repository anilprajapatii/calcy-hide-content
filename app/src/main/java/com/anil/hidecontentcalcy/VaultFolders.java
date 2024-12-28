package com.anil.hidecontentcalcy;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.view.MenuItem;
import android.widget.Toast;

public class VaultFolders extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault_folders);

        LinearLayout photosAndVideosFolder = findViewById(R.id.photosAndVideosFolder);
        LinearLayout audioFolder = findViewById(R.id.audioFolder);
        LinearLayout documentsFolder = findViewById(R.id.documentsFolder);


        photosAndVideosFolder.setOnClickListener(v -> {
            Intent intent = new Intent(this, Vault.class);
            intent.putExtra("category", "PHOTO_AND_VIDEO");
            startActivity(intent);
        });

        audioFolder.setOnClickListener(v -> {
            Intent intent = new Intent(this, Vault.class);
            intent.putExtra("category", "AUDIO");
            startActivity(intent);
        });

        documentsFolder.setOnClickListener(v -> {
            Intent intent = new Intent(this, Vault.class);
            intent.putExtra("category", "DOCUMENT");
            startActivity(intent);
        });


        findViewById(R.id.gearIcon).setOnClickListener(v -> {
            Intent intent = new Intent(VaultFolders.this, PassCode.class);
            intent.putExtra("passcode_from_vault_folder", true);
            startActivity(intent);
        });



        // ICONS FOR CONTENT HIDING

        findViewById(R.id.addPhotos).setOnClickListener(v -> {
            Intent intent = new Intent(VaultFolders.this, Gallery.class);
            intent.putExtra("run_for_images", true);
            startActivity(intent);
        });

        findViewById(R.id.addVideos).setOnClickListener(v -> {
            Intent intent = new Intent(VaultFolders.this, Gallery.class);
            intent.putExtra("run_for_videos", true);
            startActivity(intent);
        });

        findViewById(R.id.addAudio).setOnClickListener(v -> {
            Intent intent = new Intent(VaultFolders.this, Gallery.class);
            intent.putExtra("run_for_audio", true);
            startActivity(intent);
        });

        findViewById(R.id.addDocument).setOnClickListener(v -> {
            Intent intent = new Intent(VaultFolders.this, Gallery.class);
            intent.putExtra("run_for_documents", true);
            startActivity(intent);
        });

    }
}
