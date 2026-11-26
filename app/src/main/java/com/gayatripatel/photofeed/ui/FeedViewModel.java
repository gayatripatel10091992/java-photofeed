package com.gayatripatel.photofeed.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.gayatripatel.photofeed.core.Post;
import com.gayatripatel.photofeed.data.FeedRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps the ids of the pages loaded so far and maps them to the repository's current posts, so a like on one
 * screen shows up everywhere without reloading pages.
 */
public final class FeedViewModel extends ViewModel {
    public static final int PAGE_SIZE = 5;

    public static final class FeedState {
        public final List<Post> posts;
        public final boolean loading;
        public final boolean endReached;

        FeedState(List<Post> posts, boolean loading, boolean endReached) {
            this.posts = posts;
            this.loading = loading;
            this.endReached = endReached;
        }
    }

    private final FeedRepository repository;
    private final List<String> loadedIds = new ArrayList<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MediatorLiveData<FeedState> state = new MediatorLiveData<>();
    private String cursor;
    private boolean endReached;

    public FeedViewModel() {
        this(FeedRepository.get());
    }

    FeedViewModel(FeedRepository repository) {
        this.repository = repository;
        state.addSource(repository.posts(), p -> publish());
        state.addSource(loading, l -> publish());
        loadMore();
    }

    public LiveData<FeedState> state() {
        return state;
    }

    public void loadMore() {
        if (Boolean.TRUE.equals(loading.getValue()) || endReached) return;
        loading.setValue(true);
        repository.loadPage(cursor, PAGE_SIZE, page -> {
            for (Post p : page.items) if (!loadedIds.contains(p.id)) loadedIds.add(p.id);
            cursor = page.nextCursor;
            endReached = cursor == null;
            loading.setValue(false);
        });
    }

    public void refresh() {
        loadedIds.clear();
        cursor = null;
        endReached = false;
        loading.setValue(false);
        loadMore();
    }

    public void setLiked(Post post, boolean liked) {
        repository.setLiked(post.id, liked);
    }

    public void toggleSaved(Post post) {
        repository.toggleSaved(post.id);
    }

    private void publish() {
        Map<String, Post> byId = new HashMap<>();
        List<Post> all = repository.posts().getValue();
        if (all != null) for (Post p : all) byId.put(p.id, p);
        List<Post> visible = new ArrayList<>();
        // Posts created on this device appear at the top immediately.
        if (all != null) for (Post p : all) if (p.id.startsWith("new") && !loadedIds.contains(p.id)) visible.add(p);
        for (String id : loadedIds) {
            Post p = byId.get(id);
            if (p != null) visible.add(p);
        }
        state.setValue(new FeedState(visible, Boolean.TRUE.equals(loading.getValue()), endReached));
    }
}
