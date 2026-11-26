package com.gayatripatel.photofeed;

import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Renders an activity's window to a PNG on the JVM (Robolectric native graphics). CI copies the files
 * into docs/screenshots so the README always shows the current UI.
 */
public final class Screenshots {
    private Screenshots() {
    }

    /** Measures and lays out the window at screen size without saving, e.g. before scrolling a list. */
    public static void layout(Activity activity) {
        shadowOf(Looper.getMainLooper()).idle();
        View root = activity.getWindow().getDecorView();
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        root.measure(
                View.MeasureSpec.makeMeasureSpec(dm.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(dm.heightPixels, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, dm.widthPixels, dm.heightPixels);
        shadowOf(Looper.getMainLooper()).idle();
    }

    public static void capture(Activity activity, String name) {
        shadowOf(Looper.getMainLooper()).idle();
        View root = activity.getWindow().getDecorView();
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        root.measure(
                View.MeasureSpec.makeMeasureSpec(dm.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(dm.heightPixels, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, dm.widthPixels, dm.heightPixels);
        shadowOf(Looper.getMainLooper()).idle();

        Bitmap bitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(bitmap));
        File out = new File("build/outputs/screenshots/" + name + ".png");
        //noinspection ResultOfMethodCallIgnored
        out.getParentFile().mkdirs();
        try (FileOutputStream stream = new FileOutputStream(out)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } catch (IOException e) {
            throw new AssertionError("Could not write " + out, e);
        }
    }
}
