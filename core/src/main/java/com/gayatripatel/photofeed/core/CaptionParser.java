package com.gayatripatel.photofeed.core;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds #hashtags and @mentions in captions and comments, for highlighting and tag search. */
public final class CaptionParser {
    public enum Kind { HASHTAG, MENTION }

    public static final class Span {
        public final Kind kind;
        public final int start;
        public final int end;
        /** Tag or username without the leading symbol, lower-cased for hashtags. */
        public final String value;

        Span(Kind kind, int start, int end, String value) {
            this.kind = kind;
            this.start = start;
            this.end = end;
            this.value = value;
        }
    }

    // A tag must start the text or follow a non-word character, so "email@example.com" isn't a mention.
    private static final Pattern TOKEN = Pattern.compile("(?<![\\w@#])([#@])([\\p{L}\\p{N}_][\\p{L}\\p{N}_.]{0,29})");

    private CaptionParser() {
    }

    public static List<Span> parse(String text) {
        List<Span> spans = new ArrayList<>();
        if (text == null) return spans;
        Matcher m = TOKEN.matcher(text);
        while (m.find()) {
            String value = m.group(2);
            int end = m.end();
            while (value.endsWith(".")) { // trailing dots end the sentence, not the username
                value = value.substring(0, value.length() - 1);
                end--;
            }
            boolean hashtag = "#".equals(m.group(1));
            spans.add(new Span(hashtag ? Kind.HASHTAG : Kind.MENTION, m.start(), end,
                    hashtag ? value.toLowerCase(Locale.ROOT) : value));
        }
        return spans;
    }

    public static Set<String> hashtags(String text) {
        Set<String> tags = new LinkedHashSet<>();
        for (Span s : parse(text)) if (s.kind == Kind.HASHTAG) tags.add(s.value);
        return tags;
    }
}
