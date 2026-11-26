package com.gayatripatel.photofeed.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.gayatripatel.photofeed.R;
import com.gayatripatel.photofeed.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_FEED = "feed";
    private static final String TAG_PROFILE = "profile";
    public static final String EXTRA_TAB = "tab";

    private ActivityMainBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_create) {
                startActivity(new Intent(this, CreatePostActivity.class));
                return false; // keep the current tab selected
            }
            show(id == R.id.nav_profile ? TAG_PROFILE : TAG_FEED);
            return true;
        });
        if (savedInstanceState == null) {
            boolean profile = TAG_PROFILE.equals(getIntent().getStringExtra(EXTRA_TAB));
            b.bottomNav.setSelectedItemId(profile ? R.id.nav_profile : R.id.nav_home);
        }
    }

    /** Fragments are kept alive and shown/hidden so feed scroll position survives tab switches. */
    private void show(String tag) {
        Fragment target = getSupportFragmentManager().findFragmentByTag(tag);
        androidx.fragment.app.FragmentTransaction tx = getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        for (Fragment f : getSupportFragmentManager().getFragments()) if (f != target) tx.hide(f);
        if (target == null) {
            tx.add(R.id.container, TAG_PROFILE.equals(tag) ? new ProfileFragment() : new FeedFragment(), tag);
        } else {
            tx.show(target);
        }
        tx.commitNow();
    }
}
