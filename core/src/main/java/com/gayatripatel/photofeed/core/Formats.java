package com.gayatripatel.photofeed.core;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formats {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d", Locale.US);
    private static final DateTimeFormatter DATE_YEAR = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);

    private Formats() {
    }

    /** 999 → "999", 1_250 → "1.2K", 15_300 → "15.3K", 2_000_000 → "2M". Truncates, never rounds up. */
    public static String count(long n) {
        if (n < 1_000) return Long.toString(n);
        if (n < 1_000_000) return compact(n, 1_000, "K");
        if (n < 1_000_000_000) return compact(n, 1_000_000, "M");
        return compact(n, 1_000_000_000, "B");
    }

    private static String compact(long n, long unit, String suffix) {
        long whole = n / unit;
        long tenth = (n % unit) * 10 / unit;
        if (whole >= 100 || tenth == 0) return whole + suffix;
        return whole + "." + tenth + suffix;
    }

    /** Feed-style ages: "now", "5m", "3h", "2d", "4w", then a date. */
    public static String age(long thenMillis, long nowMillis, ZoneId zone) {
        long seconds = Math.max(0, (nowMillis - thenMillis) / 1000);
        if (seconds < 60) return "now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h";
        long days = hours / 24;
        if (days < 7) return days + "d";
        if (days < 28) return (days / 7) + "w";
        Instant then = Instant.ofEpochMilli(thenMillis);
        boolean sameYear = then.atZone(zone).getYear() == Instant.ofEpochMilli(nowMillis).atZone(zone).getYear();
        return (sameYear ? DATE : DATE_YEAR).format(then.atZone(zone));
    }

    /** "Liked by 1 person" / "1,204 likes" style label. */
    public static String likes(int count) {
        if (count == 0) return "Be the first to like this";
        if (count == 1) return "1 like";
        return String.format(Locale.US, "%,d likes", count);
    }
}
