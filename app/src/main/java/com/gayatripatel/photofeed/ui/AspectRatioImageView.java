package com.gayatripatel.photofeed.ui;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

/** ImageView whose height follows its width, so feed rows reserve the right space before the photo loads. */
public class AspectRatioImageView extends AppCompatImageView {
    private float ratio = 1f;

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Width divided by height. */
    public void setAspectRatio(float ratio) {
        if (ratio > 0 && ratio != this.ratio) {
            this.ratio = ratio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = Math.round(width / ratio);
        setMeasuredDimension(width, height);
    }
}
