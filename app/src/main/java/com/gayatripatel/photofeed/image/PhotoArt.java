package com.gayatripatel.photofeed.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import java.util.Random;

/**
 * Procedural "photos" (sky, sun and layered hills) and avatars, generated from a seed. They stand in for a CDN in
 * the demo and make screenshot tests deterministic.
 */
public final class PhotoArt {
    private static final int[][] PALETTES = {
            {0xFF0F2027, 0xFF2C5364, 0xFFFFC371, 0xFF1B3A4B},
            {0xFFFF7E5F, 0xFFFEB47B, 0xFFFFF1C1, 0xFF6A3093},
            {0xFF2193B0, 0xFF6DD5ED, 0xFFFFFFFF, 0xFF0B486B},
            {0xFF8E2DE2, 0xFFFF6A88, 0xFFFFE29F, 0xFF3A1C71},
            {0xFF134E5E, 0xFF71B280, 0xFFF9D423, 0xFF0B3D2E},
            {0xFFED4264, 0xFFFFEDBC, 0xFFFFFFFF, 0xFF8E0E00},
            {0xFF355C7D, 0xFFC06C84, 0xFFF8B195, 0xFF2A363B},
    };

    private PhotoArt() {
    }

    public static Bitmap photo(int seed, int width, int height) {
        Random r = new Random(seed);
        int[] p = PALETTES[Math.abs(seed) % PALETTES.length];
        Bitmap bitmap = Bitmap.createBitmap(Math.max(1, width), Math.max(1, height), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setShader(new LinearGradient(0, 0, 0, height, p[0], p[1], Shader.TileMode.CLAMP));
        c.drawRect(0, 0, width, height, paint);

        float sunX = width * (0.2f + r.nextFloat() * 0.6f);
        float sunY = height * (0.25f + r.nextFloat() * 0.2f);
        float sunR = width * (0.08f + r.nextFloat() * 0.08f);
        paint.setShader(new RadialGradient(sunX, sunY, sunR * 3, (p[2] & 0x00FFFFFF) | 0x66000000, 0x00FFFFFF, Shader.TileMode.CLAMP));
        c.drawCircle(sunX, sunY, sunR * 3, paint);
        paint.setShader(null);
        paint.setColor(p[2]);
        c.drawCircle(sunX, sunY, sunR, paint);

        int layers = 3;
        for (int i = 0; i < layers; i++) {
            float base = height * (0.55f + 0.14f * i);
            Path path = new Path();
            path.moveTo(0, height);
            path.lineTo(0, base);
            int peaks = 3 + r.nextInt(3);
            for (int k = 1; k <= peaks; k++) {
                float x = width * k / (float) peaks;
                float peak = base - height * (0.06f + r.nextFloat() * 0.16f) / (i + 1);
                path.quadTo(x - width / (2f * peaks), peak, x, base + r.nextFloat() * height * 0.03f);
            }
            path.lineTo(width, height);
            path.close();
            float shade = 0.35f + 0.25f * i;
            paint.setColor(blend(p[1], p[3], shade));
            c.drawPath(path, paint);
        }
        return bitmap;
    }

    public static Bitmap avatar(int seed, String name, int size) {
        int[] p = PALETTES[Math.abs(seed) % PALETTES.length];
        Bitmap bitmap = Bitmap.createBitmap(Math.max(1, size), Math.max(1, size), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(0, 0, size, size, p[0], p[1], Shader.TileMode.CLAMP));
        c.drawRect(0, 0, size, size, paint);
        paint.setShader(null);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(size * 0.42f);
        paint.setFakeBoldText(true);
        String initial = name == null || name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        Paint.FontMetrics fm = paint.getFontMetrics();
        c.drawText(initial, size / 2f, size / 2f - (fm.ascent + fm.descent) / 2f, paint);
        return bitmap;
    }

    private static int blend(int a, int b, float t) {
        int r = (int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t);
        int g = (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t);
        int bl = (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t);
        return Color.rgb(r, g, bl);
    }
}
