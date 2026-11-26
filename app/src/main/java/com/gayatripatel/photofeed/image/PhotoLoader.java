package com.gayatripatel.photofeed.image;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import androidx.annotation.VisibleForTesting;
import com.gayatripatel.photofeed.R;
import com.gayatripatel.photofeed.core.BitmapSampling;
import com.gayatripatel.photofeed.core.Post;
import com.gayatripatel.photofeed.core.User;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Minimal image loader for the feed: decodes/generates off the main thread, caches in an LruCache sized to 1/8 of
 * the app's heap, and tags each ImageView with its request key so a recycled row never shows the wrong photo.
 */
public final class PhotoLoader {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static Executor executor = Executors.newFixedThreadPool(2);

    private static final LruCache<String, Bitmap> CACHE =
            new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8)) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount() / 1024;
                }
            };

    private PhotoLoader() {
    }

    @VisibleForTesting
    public static void setExecutorForTests(Executor direct) {
        executor = direct;
    }

    public static void loadPost(ImageView view, Post post, int widthPx) {
        int w = Math.max(1, widthPx);
        int h = Math.round(w / post.aspectRatio);
        String key = (post.localPath != null ? post.localPath : "seed:" + post.photoSeed) + "@" + w + "x" + h;
        load(view, key, () -> post.localPath != null ? decodeFile(post.localPath, w, h) : PhotoArt.photo(post.photoSeed, w, h));
    }

    public static void loadAvatar(ImageView view, User user, int sizePx) {
        int s = Math.max(1, sizePx);
        load(view, "avatar:" + user.id + "@" + s, () -> PhotoArt.avatar(user.avatarSeed, user.displayName, s));
    }

    private interface Source {
        Bitmap produce();
    }

    private static void load(ImageView view, String key, Source source) {
        view.setTag(R.id.photo_request, key);
        Bitmap cached = CACHE.get(key);
        if (cached != null) {
            view.setImageBitmap(cached);
            return;
        }
        view.setImageDrawable(null);
        executor.execute(() -> {
            Bitmap bitmap = source.produce();
            if (bitmap == null) return;
            CACHE.put(key, bitmap);
            MAIN.post(() -> {
                if (key.equals(view.getTag(R.id.photo_request))) view.setImageBitmap(bitmap);
            });
        });
    }

    private static Bitmap decodeFile(String path, int reqW, int reqH) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = BitmapSampling.inSampleSize(bounds.outWidth, bounds.outHeight, reqW, reqH);
        return BitmapFactory.decodeFile(path, opts);
    }

    /** Result of decoding a picked photo, with the numbers the create screen shows. */
    public static final class Decoded {
        public final Bitmap bitmap;
        public final int sourceWidth;
        public final int sourceHeight;
        public final int sampleSize;

        Decoded(Bitmap bitmap, int sourceWidth, int sourceHeight, int sampleSize) {
            this.bitmap = bitmap;
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
            this.sampleSize = sampleSize;
        }
    }

    /** Two-pass decode of a picked photo: read the size, then decode at the smallest power-of-two that fits. */
    public static Decoded decodeSampled(ContentResolver resolver, Uri uri, int reqW, int reqH) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        int sample = BitmapSampling.inSampleSize(bounds.outWidth, bounds.outHeight, reqW, reqH);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        try (InputStream in = resolver.openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, opts);
            if (bitmap == null) throw new IOException("Unsupported image");
            return new Decoded(bitmap, bounds.outWidth, bounds.outHeight, sample);
        }
    }
}
