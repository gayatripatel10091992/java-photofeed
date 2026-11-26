package com.gayatripatel.photofeed.data;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.gayatripatel.photofeed.core.Comment;
import com.gayatripatel.photofeed.core.FeedPager;
import com.gayatripatel.photofeed.core.Post;
import com.gayatripatel.photofeed.core.SampleFeed;
import com.gayatripatel.photofeed.core.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * In-memory stand-in for the posts/users/comments API. Pages are served with simulated latency through the same
 * cursor pagination a backend would use; likes and saves update optimistically.
 */
public final class FeedRepository {
    private static FeedRepository instance;

    public static synchronized FeedRepository get() {
        if (instance == null) instance = new FeedRepository(System.currentTimeMillis(), 450);
        return instance;
    }

    /** Fixed clock and no latency for UI tests. */
    @VisibleForTesting
    public static synchronized FeedRepository resetForTests(long now) {
        instance = new FeedRepository(now, 0);
        return instance;
    }

    private final long createdAt;
    private final long latencyMs;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Map<String, User> users = new LinkedHashMap<>();
    private final MutableLiveData<List<Post>> posts;
    private final Map<String, MutableLiveData<List<Comment>>> comments = new HashMap<>();
    private int nextId = 1;

    private FeedRepository(long now, long latencyMs) {
        this.createdAt = now;
        this.latencyMs = latencyMs;
        for (User u : SampleFeed.users()) users.put(u.id, u);
        this.posts = new MutableLiveData<>(new ArrayList<>(SampleFeed.posts(now)));
    }

    public long now() {
        return latencyMs == 0 ? createdAt : System.currentTimeMillis();
    }

    public User user(String id) {
        return users.get(id);
    }

    public User me() {
        return users.get(SampleFeed.ME);
    }

    public LiveData<List<Post>> posts() {
        return posts;
    }

    public void loadPage(String cursor, int size, Consumer<FeedPager.Page> callback) {
        Runnable deliver = () -> callback.accept(FeedPager.page(current(), cursor, size));
        if (latencyMs == 0) deliver.run();
        else main.postDelayed(deliver, latencyMs);
    }

    public List<Post> postsBy(String userId) {
        List<Post> out = new ArrayList<>();
        for (Post p : current()) if (p.authorId.equals(userId)) out.add(p);
        return out;
    }

    public Post post(String id) {
        for (Post p : current()) if (p.id.equals(id)) return p;
        return null;
    }

    public void setLiked(String postId, boolean liked) {
        update(postId, p -> p.withLike(liked));
    }

    public void toggleSaved(String postId) {
        update(postId, p -> p.withSaved(!p.savedByMe));
    }

    public Post addPost(String localPath, float aspectRatio, String caption, String location) {
        Post post = new Post("new" + (nextId++), SampleFeed.ME, 0, localPath, aspectRatio, caption,
                location == null || location.trim().isEmpty() ? null : location.trim(), now(), 0, 0, false, false);
        List<Post> next = new ArrayList<>(current());
        next.add(0, post);
        posts.setValue(next);
        return post;
    }

    public LiveData<List<Comment>> comments(String postId) {
        MutableLiveData<List<Comment>> live = comments.get(postId);
        if (live == null) {
            live = new MutableLiveData<>(new ArrayList<>(SampleFeed.comments(postId, now())));
            comments.put(postId, live);
        }
        return live;
    }

    public void addComment(String postId, String text) {
        MutableLiveData<List<Comment>> live = (MutableLiveData<List<Comment>>) comments(postId);
        List<Comment> next = new ArrayList<>(live.getValue());
        next.add(new Comment("c" + (nextId++), postId, SampleFeed.ME, text, now()));
        live.setValue(next);
        update(postId, Post::withCommentAdded);
    }

    private List<Post> current() {
        List<Post> list = posts.getValue();
        return list == null ? new ArrayList<>() : list;
    }

    private interface Change {
        Post apply(Post p);
    }

    private void update(String postId, Change change) {
        List<Post> next = new ArrayList<>(current());
        for (int i = 0; i < next.size(); i++) {
            if (next.get(i).id.equals(postId)) {
                next.set(i, change.apply(next.get(i)));
                posts.setValue(next);
                return;
            }
        }
    }
}
