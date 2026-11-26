package com.gayatripatel.photofeed.core;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Fictional users, posts and comments. Photos are generated art, so the demo works offline. */
public final class SampleFeed {
    public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    public static final long FIXED_NOW = ZonedDateTime.of(2026, 11, 20, 18, 30, 0, 0, ZONE).toInstant().toEpochMilli();
    public static final String ME = "u0";

    private static final long MIN = 60_000L;
    private static final long HOUR = 60 * MIN;
    private static final long DAY = 24 * HOUR;

    private SampleFeed() {
    }

    public static List<User> users() {
        return Arrays.asList(
                new User(ME, "gayatri.codes", "Gayatri", "Android engineer · Vadodara · coffee, Kotlin and sunsets", 1_284, 312, false, 11),
                new User("u1", "wanderlust.anya", "Anya Kapoor", "Mountains > beaches", 48_210, 512, true, 23),
                new User("u2", "chef.rohan", "Rohan Mehta", "Home cooking, slow food", 9_870, 803, false, 37),
                new User("u3", "lens.and.light", "Meera Iyer", "Street photography", 152_000, 211, true, 41),
                new User("u4", "kabir.runs", "Kabir Shah", "Marathons and early mornings", 3_402, 690, false, 53),
                new User("u5", "studio.nila", "Nila Studio", "Ceramics & clay", 21_500, 145, false, 67));
    }

    public static List<Post> posts(long now) {
        List<Post> list = new ArrayList<>();
        list.add(new Post("p1", "u1", 101, null, 0.8f, "Golden hour above the clouds at Kedarkantha ⛰️ #himalayas #trekking with @kabir.runs", "Kedarkantha, Uttarakhand", now - 2 * HOUR, 12_408, 214, false, false));
        list.add(new Post("p2", "u2", 202, null, 1.0f, "Sunday dal makhani, slow-cooked for 8 hours #homecooking #comfortfood", "Mumbai", now - 5 * HOUR, 1_932, 87, true, true));
        list.add(new Post("p3", "u3", 303, null, 1.25f, "Monsoon reflections in the old city #streetphotography #rain", "Ahmedabad", now - 9 * HOUR, 23_771, 402, false, false));
        list.add(new Post("p4", "u5", 404, null, 0.8f, "New glaze test: ocean blue on stoneware #ceramics #handmade", "Pune", now - DAY - 3 * HOUR, 4_120, 66, false, false));
        list.add(new Post("p5", "u4", 505, null, 1.0f, "21K done before sunrise. Thanks for pacing me @wanderlust.anya! #running #halfmarathon", "Sabarmati Riverfront", now - 2 * DAY, 812, 45, true, false));
        list.add(new Post("p6", ME, 606, null, 1.0f, "Sunset from the office terrace after shipping a release #androiddev #kotlin", "Vadodara", now - 3 * DAY, 341, 28, false, false));
        list.add(new Post("p7", "u1", 707, null, 1.6f, "Spiti valley road trip, day 3 #roadtrip", "Spiti Valley", now - 6 * DAY, 9_032, 133, false, false));
        list.add(new Post("p8", ME, 808, null, 0.8f, "Weekend pottery class with @studio.nila #ceramics", "Vadodara", now - 9 * DAY, 198, 12, false, false));
        list.add(new Post("p9", ME, 909, null, 1.0f, "Chai and code #androiddev", "Vadodara", now - 15 * DAY, 256, 19, false, false));
        for (int i = 0; i < 9; i++) {
            list.add(new Post("p" + (10 + i), ME, 1000 + i * 37, null, 1.0f, "Archive #" + (i + 1), "Vadodara", now - (20 + i * 6) * DAY, 100 + i * 13, i, false, false));
        }
        for (int i = 0; i < 12; i++) {
            String author = "u" + (1 + i % 5);
            list.add(new Post("q" + i, author, 2000 + i * 53, null, i % 3 == 0 ? 0.8f : 1.0f, "Older post " + (i + 1) + " #throwback", null, now - (30 + i) * DAY, 500 + i * 71, 3 + i, false, false));
        }
        return list;
    }

    public static List<Comment> comments(String postId, long now) {
        return Arrays.asList(
                new Comment("c1", postId, "u4", "That view! Which campsite was this? 😍", now - 90 * MIN),
                new Comment("c2", postId, "u3", "The light here is unreal. @wanderlust.anya what lens?", now - 70 * MIN),
                new Comment("c3", postId, "u1", "@lens.and.light just the phone! #nofilter", now - 40 * MIN),
                new Comment("c4", postId, ME, "Adding this to my 2027 list #himalayas", now - 12 * MIN));
    }

    /** User ids with an active story, in the order the stories row shows them. Seen stories go last. */
    public static List<String> storyOrder() {
        return Arrays.asList(ME, "u3", "u1", "u5", "u2", "u4");
    }

    public static boolean storySeen(String userId) {
        return "u2".equals(userId) || "u4".equals(userId);
    }
}
