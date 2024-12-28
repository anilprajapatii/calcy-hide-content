package com.anil.hidecontentcalcy;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.VideoView;
import androidx.collection.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class ImageAndVideoAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<String> mediaPaths;
    private final ArrayList<Boolean> isVideoList;
    private final LayoutInflater inflater;
    private final LruCache<String, Bitmap> thumbnailCache;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final Set<Target> targets;
    private static final int THUMB_SIZE = 336; // Reduced thumbnail size
    private static final int CACHE_SIZE_PERCENTAGE = 25; // Percentage of available memory for cache

    public ImageAndVideoAdapter(Context context, ArrayList<String> mediaPaths, ArrayList<Boolean> isVideoList) {
        this.context = context;
        this.mediaPaths = mediaPaths;
        this.isVideoList = isVideoList;
        this.inflater = LayoutInflater.from(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.targets = new HashSet<>();

        // Initialize thread pool for parallel processing
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(numberOfCores);

        // Increased cache size for better performance
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory * CACHE_SIZE_PERCENTAGE / 100;

        thumbnailCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (evicted && oldValue != null && !oldValue.isRecycled()) {
                    oldValue.recycle();
                }
            }
        };

        // Configure Picasso
        Picasso.get().setIndicatorsEnabled(false);
    }

    @Override
    public int getCount() {
        return mediaPaths.size();
    }

    @Override
    public Object getItem(int position) {
        return mediaPaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = convertView.findViewById(R.id.imageView);
            viewHolder.videoView = convertView.findViewById(R.id.videoView);

            // Set fixed dimensions for better performance
            viewHolder.imageView.getLayoutParams().height = THUMB_SIZE;
            viewHolder.imageView.getLayoutParams().width = THUMB_SIZE;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String mediaPath = mediaPaths.get(position);
        boolean isVideo = isVideoList.get(position);

        // Reset views
        viewHolder.imageView.setVisibility(View.VISIBLE);
        viewHolder.videoView.setVisibility(View.GONE);

        // Cancel any pending Picasso requests for this ImageView
        Picasso.get().cancelRequest(viewHolder.imageView);

        // Set loading indicator
        viewHolder.imageView.setImageResource(R.drawable.loading_spinner);

        if (isVideo) {
            loadVideoThumbnail(mediaPath, viewHolder.imageView);
        } else {
            loadImage(mediaPath, viewHolder.imageView);
        }

        return convertView;
    }

    private void loadImage(String imagePath, ImageView imageView) {
        Target target = new WeakTarget(imageView);
        targets.add(target);

        Picasso.get()
                .load("file://" + imagePath)
                .resize(THUMB_SIZE, THUMB_SIZE)
                .centerCrop()
                .priority(Picasso.Priority.HIGH)
                .into(target);
    }

    private static class WeakTarget implements Target {
        private final WeakReference<ImageView> imageViewRef;

        WeakTarget(ImageView imageView) {
            this.imageViewRef = new WeakReference<>(imageView);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            ImageView imageView = imageViewRef.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

        @Override
        public void onBitmapFailed(Exception e, android.graphics.drawable.Drawable errorDrawable) {
            ImageView imageView = imageViewRef.get();
            if (imageView != null && errorDrawable != null) {
                imageView.setImageDrawable(errorDrawable);
            }
        }

        @Override
        public void onPrepareLoad(android.graphics.drawable.Drawable placeHolderDrawable) {
            ImageView imageView = imageViewRef.get();
            if (imageView != null && placeHolderDrawable != null) {
                imageView.setImageDrawable(placeHolderDrawable);
            }
        }
    }

    private void loadVideoThumbnail(String videoPath, ImageView imageView) {
        Bitmap cachedThumbnail = thumbnailCache.get(videoPath);
        if (cachedThumbnail != null && !cachedThumbnail.isRecycled()) {
            imageView.setImageBitmap(cachedThumbnail);
            return;
        }

        executorService.execute(() -> {
            Bitmap thumbnail = generateThumbnail(videoPath);
            if (thumbnail != null) {
                thumbnailCache.put(videoPath, thumbnail);
                mainHandler.post(() -> {
                    imageView.setImageBitmap(thumbnail);
                });
            }
        });
    }

    private Bitmap generateThumbnail(String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);

            Bitmap frame = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

            if (frame == null) return null;

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                    frame,
                    THUMB_SIZE,
                    THUMB_SIZE,
                    true
            );

            if (frame != scaledBitmap) {
                frame.recycle();
            }

            return scaledBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {}
        }
    }

    private static class ViewHolder {
        ImageView imageView;
        VideoView videoView;
    }

    public void release() {
        executorService.shutdown();

        for (Target target : targets) {
            Picasso.get().cancelRequest(target);
        }
        targets.clear();

        clearCache();
    }

    public void clearCache() {
        if (thumbnailCache != null) {
            thumbnailCache.evictAll();
        }
        Picasso.get().cancelTag(this);
    }
}