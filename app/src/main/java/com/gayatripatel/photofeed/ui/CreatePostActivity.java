package com.gayatripatel.photofeed.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.gayatripatel.photofeed.R;
import com.gayatripatel.photofeed.core.BitmapSampling;
import com.gayatripatel.photofeed.core.CaptionParser;
import com.gayatripatel.photofeed.core.Post;
import com.gayatripatel.photofeed.data.FeedRepository;
import com.gayatripatel.photofeed.databinding.ActivityCreatePostBinding;
import com.gayatripatel.photofeed.image.PhotoArt;
import com.gayatripatel.photofeed.image.PhotoLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pick a photo with the system Photo Picker (no storage permission), decode it downsampled off the main thread,
 * write a 1080px JPEG to app storage, and highlight #tags and @mentions while typing the caption.
 */
public class CreatePostActivity extends AppCompatActivity {
    /** Demo/test hook: show a generated photo instead of opening the picker. */
    public static final String EXTRA_DEMO_SEED = "demo_seed";
    private static final int TARGET_PX = 1080;

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private ActivityCreatePostBinding b;
    private File savedPhoto;
    private float aspect = 1f;

    private final ActivityResultLauncher<PickVisualMediaRequest> picker =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) decodeAndSave(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCreatePostBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        b.toolbar.setNavigationOnClickListener(v -> finish());
        b.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_share) {
                share();
                return true;
            }
            return false;
        });
        b.picker.setOnClickListener(v -> picker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
        b.caption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                for (ForegroundColorSpan span : s.getSpans(0, s.length(), ForegroundColorSpan.class)) s.removeSpan(span);
                Captions.highlight(CreatePostActivity.this, s, 0, s.toString());
                Set<String> tags = CaptionParser.hashtags(s.toString());
                b.tags.setText(tags.isEmpty() ? "" : getString(R.string.tags_preview, "#" + String.join("  #", tags)));
            }
        });

        int demoSeed = getIntent().getIntExtra(EXTRA_DEMO_SEED, 0);
        if (demoSeed != 0) showPreview(PhotoArt.photo(demoSeed, TARGET_PX, TARGET_PX), null);
    }

    private void decodeAndSave(Uri uri) {
        io.execute(() -> {
            try {
                PhotoLoader.Decoded d = PhotoLoader.decodeSampled(getContentResolver(), uri, TARGET_PX, TARGET_PX);
                File dir = new File(getFilesDir(), "posts");
                if (!dir.exists() && !dir.mkdirs()) throw new IOException("Can't create " + dir);
                File out = new File(dir, System.currentTimeMillis() + ".jpg");
                try (FileOutputStream stream = new FileOutputStream(out)) {
                    d.bitmap.compress(Bitmap.CompressFormat.JPEG, 88, stream);
                }
                String info = getString(R.string.decoded_info, d.bitmap.getWidth(), d.bitmap.getHeight(), d.sampleSize,
                        mb(BitmapSampling.argbBytes(d.bitmap.getWidth(), d.bitmap.getHeight())),
                        mb(BitmapSampling.argbBytes(d.sourceWidth, d.sourceHeight)));
                main.post(() -> {
                    savedPhoto = out;
                    showPreview(d.bitmap, info);
                });
            } catch (IOException | SecurityException e) {
                main.post(() -> Toast.makeText(this, R.string.pick_photo, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private static String mb(long bytes) {
        return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void showPreview(Bitmap bitmap, String info) {
        aspect = Post.clampAspect(bitmap.getWidth() / (float) bitmap.getHeight());
        b.preview.setAspectRatio(aspect);
        b.preview.setImageBitmap(bitmap);
        b.pickHint.setVisibility(View.GONE);
        b.decodeInfo.setVisibility(info == null ? View.GONE : View.VISIBLE);
        b.decodeInfo.setText(info);
    }

    private void share() {
        if (savedPhoto == null && b.pickHint.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, R.string.pick_photo, Toast.LENGTH_SHORT).show();
            return;
        }
        FeedRepository.get().addPost(savedPhoto == null ? null : savedPhoto.getAbsolutePath(), aspect,
                b.caption.getText().toString().trim(), b.location.getText().toString());
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }
}
