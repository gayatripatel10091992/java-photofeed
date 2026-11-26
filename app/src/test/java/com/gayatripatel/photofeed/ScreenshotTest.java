package com.gayatripatel.photofeed;

import android.app.Application;
import android.content.Intent;
import android.widget.EditText;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.gayatripatel.photofeed.core.SampleFeed;
import com.gayatripatel.photofeed.data.FeedRepository;
import com.gayatripatel.photofeed.image.PhotoLoader;
import com.gayatripatel.photofeed.ui.CommentsActivity;
import com.gayatripatel.photofeed.ui.CreatePostActivity;
import com.gayatripatel.photofeed.ui.MainActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@RunWith(AndroidJUnit4.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = 34, qualifiers = "w411dp-h891dp-xhdpi", application = Application.class)
public class ScreenshotTest {

    @Before
    public void setUp() {
        FeedRepository.resetForTests(SampleFeed.FIXED_NOW);
        PhotoLoader.setExecutorForTests(Runnable::run);
    }

    private static Intent main(String tab) {
        return new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class).putExtra(MainActivity.EXTRA_TAB, tab);
    }

    @Test
    public void feed() {
        MainActivity a = Robolectric.buildActivity(MainActivity.class, main("feed")).setup().get();
        Screenshots.capture(a, "01_feed");
    }

    @Test
    @Config(qualifiers = "+night")
    public void feedDark() {
        MainActivity a = Robolectric.buildActivity(MainActivity.class, main("feed")).setup().get();
        Screenshots.capture(a, "02_feed_dark");
    }

    @Test
    public void likedAndSavedPost() {
        MainActivity a = Robolectric.buildActivity(MainActivity.class, main("feed")).setup().get();
        RecyclerView feed = a.findViewById(R.id.feed);
        ((androidx.recyclerview.widget.LinearLayoutManager) feed.getLayoutManager()).scrollToPositionWithOffset(2, 0);
        Screenshots.capture(a, "03_liked_and_saved");
    }

    @Test
    public void profile() {
        MainActivity a = Robolectric.buildActivity(MainActivity.class, main("profile")).setup().get();
        Screenshots.capture(a, "04_profile_grid");
    }

    @Test
    public void comments() {
        CommentsActivity a = Robolectric.buildActivity(CommentsActivity.class,
                CommentsActivity.intent(ApplicationProvider.getApplicationContext(), "p1")).setup().get();
        Screenshots.capture(a, "05_comments");
    }

    @Test
    public void createPost() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CreatePostActivity.class)
                .putExtra(CreatePostActivity.EXTRA_DEMO_SEED, 4242);
        CreatePostActivity a = Robolectric.buildActivity(CreatePostActivity.class, intent).setup().get();
        EditText caption = a.findViewById(R.id.caption);
        caption.setText("First light over Sabarmati with @kabir.runs #sunrise #vadodara #androiddev");
        EditText location = a.findViewById(R.id.location);
        location.setText("Vadodara, Gujarat");
        Screenshots.capture(a, "06_create_post");
    }
}
