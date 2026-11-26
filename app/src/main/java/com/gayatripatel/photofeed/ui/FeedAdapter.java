package com.gayatripatel.photofeed.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gayatripatel.photofeed.R;
import com.gayatripatel.photofeed.core.Formats;
import com.gayatripatel.photofeed.core.Post;
import com.gayatripatel.photofeed.core.User;
import com.gayatripatel.photofeed.data.FeedRepository;
import com.gayatripatel.photofeed.databinding.ItemLoadingBinding;
import com.gayatripatel.photofeed.databinding.ItemPostBinding;
import com.gayatripatel.photofeed.databinding.ItemStoriesBinding;
import com.gayatripatel.photofeed.image.PhotoLoader;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/** Stories row, then posts, then a loading/end footer. Diffs are computed synchronously: pages are small. */
final class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    interface Listener {
        void onLike(Post post, boolean liked);

        void onSave(Post post);

        void onComments(Post post);
    }

    private static final int TYPE_STORIES = 0;
    private static final int TYPE_POST = 1;
    private static final int TYPE_FOOTER = 2;

    private final FeedRepository repository;
    private final ZoneId zone;
    private final Listener listener;
    private List<Post> posts = new ArrayList<>();
    private boolean loading;
    private boolean endReached;

    FeedAdapter(FeedRepository repository, ZoneId zone, Listener listener) {
        this.repository = repository;
        this.zone = zone;
        this.listener = listener;
    }

    void submit(List<Post> next, boolean loading, boolean endReached) {
        List<Post> old = posts;
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return old.size();
            }

            @Override
            public int getNewListSize() {
                return next.size();
            }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                return old.get(o).id.equals(next.get(n).id);
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                return old.get(o) == next.get(n); // posts are immutable; a change means a new instance
            }
        });
        posts = new ArrayList<>(next);
        this.loading = loading;
        this.endReached = endReached;
        diff.dispatchUpdatesTo(new androidx.recyclerview.widget.ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position + 1, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position + 1, count);
            }

            @Override
            public void onMoved(int from, int to) {
                notifyItemMoved(from + 1, to + 1);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                notifyItemRangeChanged(position + 1, count, payload);
            }
        });
        notifyItemChanged(getItemCount() - 1);
    }

    int postCount() {
        return posts.size();
    }

    @Override
    public int getItemCount() {
        return posts.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_STORIES;
        if (position == getItemCount() - 1) return TYPE_FOOTER;
        return TYPE_POST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_STORIES) {
            ItemStoriesBinding b = ItemStoriesBinding.inflate(inflater, parent, false);
            b.stories.setLayoutManager(new LinearLayoutManager(parent.getContext(), LinearLayoutManager.HORIZONTAL, false));
            b.stories.setAdapter(new StoriesAdapter(repository));
            return new RecyclerView.ViewHolder(b.getRoot()) {
            };
        }
        if (viewType == TYPE_FOOTER) return new FooterHolder(ItemLoadingBinding.inflate(inflater, parent, false));
        return new PostHolder(ItemPostBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostHolder) ((PostHolder) holder).bind(posts.get(position - 1));
        if (holder instanceof FooterHolder) {
            FooterHolder f = (FooterHolder) holder;
            f.b.progress.setVisibility(loading && !endReached ? View.VISIBLE : View.GONE);
            f.b.end.setVisibility(endReached ? View.VISIBLE : View.GONE);
        }
    }

    private static final class FooterHolder extends RecyclerView.ViewHolder {
        final ItemLoadingBinding b;

        FooterHolder(ItemLoadingBinding binding) {
            super(binding.getRoot());
            b = binding;
        }
    }

    final class PostHolder extends RecyclerView.ViewHolder {
        private final ItemPostBinding b;
        private Post post;

        @SuppressLint("ClickableViewAccessibility")
        PostHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            b = binding;
            // Double-tap the photo to like (never unlikes), with the heart pop animation.
            GestureDetector detector = new GestureDetector(binding.getRoot().getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(@NonNull MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onDoubleTap(@NonNull MotionEvent e) {
                    if (post == null) return false;
                    popHeart();
                    if (!post.likedByMe) listener.onLike(post, true);
                    return true;
                }
            });
            b.photo.setOnTouchListener((v, event) -> detector.onTouchEvent(event));
            b.like.setOnClickListener(v -> {
                if (post != null) listener.onLike(post, !post.likedByMe);
            });
            b.save.setOnClickListener(v -> {
                if (post != null) listener.onSave(post);
            });
            View.OnClickListener comments = v -> {
                if (post != null) listener.onComments(post);
            };
            b.comment.setOnClickListener(comments);
            b.viewComments.setOnClickListener(comments);
        }

        void bind(Post p) {
            post = p;
            Context c = b.getRoot().getContext();
            User author = repository.user(p.authorId);
            b.username.setText(author.username);
            b.username.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, author.verified ? R.drawable.ic_verified : 0, 0);
            b.location.setText(p.location);
            b.location.setVisibility(p.location == null ? View.GONE : View.VISIBLE);
            int avatarPx = c.getResources().getDimensionPixelSize(R.dimen.avatar_small);
            PhotoLoader.loadAvatar(b.authorAvatar, author, avatarPx);

            b.photo.setAspectRatio(p.aspectRatio);
            PhotoLoader.loadPost(b.photo, p, c.getResources().getDisplayMetrics().widthPixels);

            b.like.setImageResource(p.likedByMe ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
            b.like.setContentDescription(c.getString(p.likedByMe ? R.string.unlike : R.string.like));
            b.save.setImageResource(p.savedByMe ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark);
            b.likes.setText(Formats.likes(p.likeCount));
            b.caption.setText(Captions.build(c, author.username, p.caption));
            b.viewComments.setText(c.getString(R.string.view_comments, p.commentCount));
            b.viewComments.setVisibility(p.commentCount > 0 ? View.VISIBLE : View.GONE);
            b.age.setText(Formats.age(p.createdAtMillis, repository.now(), zone));
        }

        private void popHeart() {
            b.heartOverlay.animate().cancel();
            b.heartOverlay.setScaleX(0.4f);
            b.heartOverlay.setScaleY(0.4f);
            b.heartOverlay.setAlpha(1f);
            b.heartOverlay.animate().scaleX(1f).scaleY(1f).setDuration(180)
                    .withEndAction(() -> b.heartOverlay.animate().alpha(0f).setStartDelay(350).setDuration(200))
                    .start();
        }
    }
}
