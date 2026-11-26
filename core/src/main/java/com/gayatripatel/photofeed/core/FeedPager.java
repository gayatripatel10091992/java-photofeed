package com.gayatripatel.photofeed.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cursor pagination over a newest-first feed. The cursor is the id of the last post on the previous page, so
 * posts added at the top while the user scrolls don't shift later pages (no duplicates, nothing skipped).
 */
public final class FeedPager {

    public static final class Page {
        public final List<Post> items;
        /** Null when there are no more pages. */
        public final String nextCursor;

        Page(List<Post> items, String nextCursor) {
            this.items = Collections.unmodifiableList(items);
            this.nextCursor = nextCursor;
        }
    }

    private FeedPager() {
    }

    public static Page page(List<Post> newestFirst, String cursor, int size) {
        int start = 0;
        if (cursor != null) {
            start = newestFirst.size();
            for (int i = 0; i < newestFirst.size(); i++) {
                if (newestFirst.get(i).id.equals(cursor)) {
                    start = i + 1;
                    break;
                }
            }
        }
        int end = Math.min(newestFirst.size(), start + size);
        List<Post> items = new ArrayList<>(newestFirst.subList(start, end));
        String next = end < newestFirst.size() && !items.isEmpty() ? items.get(items.size() - 1).id : null;
        return new Page(items, next);
    }

    /** Start loading the next page when the user is within this many items of the end. */
    public static boolean shouldPrefetch(int lastVisiblePosition, int itemCount, int threshold) {
        return itemCount > 0 && lastVisiblePosition >= itemCount - 1 - threshold;
    }
}
