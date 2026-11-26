package com.gayatripatel.photofeed.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class CoreTest {
    private final long now = SampleFeed.FIXED_NOW;

    @Test
    public void compactCounts() {
        assertEquals("999", Formats.count(999));
        assertEquals("1K", Formats.count(1_000));
        assertEquals("1.2K", Formats.count(1_299));
        assertEquals("152K", Formats.count(152_000));
        assertEquals("2.5M", Formats.count(2_550_000));
        assertEquals("12,408 likes", Formats.likes(12_408));
        assertEquals("1 like", Formats.likes(1));
    }

    @Test
    public void ages() {
        assertEquals("now", Formats.age(now - 20_000, now, SampleFeed.ZONE));
        assertEquals("12m", Formats.age(now - 12 * 60_000, now, SampleFeed.ZONE));
        assertEquals("5h", Formats.age(now - 5 * 3_600_000, now, SampleFeed.ZONE));
        assertEquals("3d", Formats.age(now - 3 * 86_400_000L, now, SampleFeed.ZONE));
        assertEquals("2w", Formats.age(now - 15 * 86_400_000L, now, SampleFeed.ZONE));
        assertEquals("Oct 1", Formats.age(now - 50 * 86_400_000L, now, SampleFeed.ZONE));
    }

    @Test
    public void captionTokens() {
        List<CaptionParser.Span> spans = CaptionParser.parse("Thanks @kabir.runs! #Running #halfMarathon, mail me at a@b.com.");
        assertEquals(3, spans.size());
        assertEquals("kabir.runs", spans.get(0).value);
        assertEquals("running", spans.get(1).value);
        assertEquals("halfmarathon", spans.get(2).value);
        assertEquals("@kabir.runs", "Thanks @kabir.runs! #Running".substring(spans.get(0).start, spans.get(0).end));
        assertEquals("[himalayas, trekking]", CaptionParser.hashtags(SampleFeed.posts(now).get(0).caption).toString());
        assertTrue(CaptionParser.parse("no tags here").isEmpty());
    }

    @Test
    public void mentionAtEndOfSentenceDropsTheDot() {
        List<CaptionParser.Span> spans = CaptionParser.parse("Shot by @lens.and.light.");
        assertEquals("lens.and.light", spans.get(0).value);
    }

    @Test
    public void cursorPagination() {
        List<Post> all = SampleFeed.posts(now);
        FeedPager.Page first = FeedPager.page(all, null, 5);
        assertEquals("p5", first.nextCursor);
        FeedPager.Page second = FeedPager.page(all, first.nextCursor, 5);
        assertEquals("p6", second.items.get(0).id);

        // A new post at the top must not shift page two.
        List<Post> withNew = new ArrayList<>(all);
        withNew.add(0, all.get(3).withSaved(true));
        assertEquals("p6", FeedPager.page(withNew, first.nextCursor, 5).items.get(0).id);

        FeedPager.Page last = FeedPager.page(all, all.get(all.size() - 2).id, 5);
        assertEquals(1, last.items.size());
        assertNull(last.nextCursor);
        assertTrue(FeedPager.shouldPrefetch(7, 10, 3));
        assertFalse(FeedPager.shouldPrefetch(2, 10, 3));
    }

    @Test
    public void likesAreIdempotentAndNeverNegative() {
        Post p = SampleFeed.posts(now).get(0);
        Post liked = p.withLike(true);
        assertEquals(p.likeCount + 1, liked.likeCount);
        assertSame(liked, liked.withLike(true));
        assertEquals(p.likeCount, liked.withLike(false).likeCount);
        Post zero = new Post("x", "u1", 1, null, 1f, "", null, 0, 0, 0, true, false);
        assertEquals(0, zero.withLike(false).likeCount);
        assertEquals(0.8f, new Post("y", "u1", 1, null, 0.3f, "", null, 0, 0, 0, false, false).aspectRatio, 0.0001f);
    }

    @Test
    public void sampling() {
        assertEquals(1, BitmapSampling.inSampleSize(1080, 1080, 1080, 1080));
        assertEquals(2, BitmapSampling.inSampleSize(4032, 3024, 1080, 720)); // 2016×1512 still covers 1080×720
        assertEquals(8, BitmapSampling.inSampleSize(4032, 3024, 500, 375));
        assertEquals(2, BitmapSampling.inSampleSize(4000, 3000, 1500, 1500));
        assertEquals(48_771_072L, BitmapSampling.argbBytes(4032, 3024));
    }
}
