package com.gayatripatel.photofeed.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import com.gayatripatel.photofeed.R;
import com.gayatripatel.photofeed.core.CaptionParser;

/** "username caption" with a bold username and coloured #hashtags and @mentions. */
final class Captions {
    private Captions() {
    }

    static CharSequence build(Context context, String username, String text) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        if (username != null) {
            sb.append(username, new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.append(' ');
        }
        int offset = sb.length();
        sb.append(text == null ? "" : text);
        highlight(context, sb, offset, text);
        return sb;
    }

    static void highlight(Context context, android.text.Spannable target, int offset, String text) {
        int color = linkColor(context);
        for (CaptionParser.Span span : CaptionParser.parse(text)) {
            target.setSpan(new ForegroundColorSpan(color), offset + span.start, offset + span.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    static int linkColor(Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.pfLinkColor});
        try {
            return a.getColor(0, 0xFF00376B);
        } finally {
            a.recycle();
        }
    }
}
