package com.gayatripatel.photofeed.core;

/**
 * Picks BitmapFactory's {@code inSampleSize} so a 12-megapixel camera photo is decoded at roughly the size it's
 * shown, instead of allocating ~48 MB for a full-resolution ARGB bitmap.
 */
public final class BitmapSampling {
    private BitmapSampling() {
    }

    /** Largest power of two that keeps both decoded dimensions at or above the requested size. */
    public static int inSampleSize(int srcWidth, int srcHeight, int reqWidth, int reqHeight) {
        int sample = 1;
        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            int halfH = srcHeight / 2;
            int halfW = srcWidth / 2;
            while (halfH / sample >= reqHeight && halfW / sample >= reqWidth) sample *= 2;
        }
        return sample;
    }

    public static long argbBytes(int width, int height) {
        return 4L * width * height;
    }
}
