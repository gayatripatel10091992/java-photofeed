package com.gayatripatel.photofeed.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gayatripatel.photofeed.R;
import com.gayatripatel.photofeed.core.Comment;
import com.gayatripatel.photofeed.core.Formats;
import com.gayatripatel.photofeed.core.Post;
import com.gayatripatel.photofeed.core.SampleFeed;
import com.gayatripatel.photofeed.core.User;
import com.gayatripatel.photofeed.data.FeedRepository;
import com.gayatripatel.photofeed.databinding.ActivityCommentsBinding;
import com.gayatripatel.photofeed.databinding.ItemCommentBinding;
import com.gayatripatel.photofeed.image.PhotoLoader;
import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {
    private static final String EXTRA_POST = "post_id";

    public static Intent intent(Context context, String postId) {
        return new Intent(context, CommentsActivity.class).putExtra(EXTRA_POST, postId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCommentsBinding b = ActivityCommentsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        b.toolbar.setNavigationOnClickListener(v -> finish());

        FeedRepository repository = FeedRepository.get();
        String postId = getIntent().getStringExtra(EXTRA_POST);
        Post post = repository.post(postId);
        if (post == null) {
            finish();
            return;
        }
        CommentsAdapter adapter = new CommentsAdapter(repository, post);
        b.list.setLayoutManager(new LinearLayoutManager(this));
        b.list.setAdapter(adapter);
        PhotoLoader.loadAvatar(b.myAvatar, repository.me(), getResources().getDimensionPixelSize(R.dimen.avatar_small));

        repository.comments(postId).observe(this, comments -> {
            adapter.submit(comments);
            b.list.scrollToPosition(adapter.getItemCount() - 1);
        });
        b.post.setOnClickListener(v -> {
            String text = b.input.getText().toString().trim();
            if (text.isEmpty()) return;
            repository.addComment(postId, text);
            b.input.setText("");
        });
    }

    private static final class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.Holder> {
        private final FeedRepository repository;
        private final Post post;
        private final List<Comment> items = new ArrayList<>();

        CommentsAdapter(FeedRepository repository, Post post) {
            this.repository = repository;
            this.post = post;
        }

        @android.annotation.SuppressLint("NotifyDataSetChanged")
        void submit(List<Comment> comments) {
            items.clear();
            // The caption is shown first, like the original poster's comment.
            items.add(new Comment("caption", post.id, post.authorId, post.caption, post.createdAtMillis));
            items.addAll(comments);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            Comment c = items.get(position);
            User author = repository.user(c.authorId);
            Context ctx = h.itemView.getContext();
            h.b.text.setText(Captions.build(ctx, author.username, c.text));
            h.b.age.setText(Formats.age(c.createdAtMillis, repository.now(), SampleFeed.ZONE));
            PhotoLoader.loadAvatar(h.b.avatar, author, ctx.getResources().getDimensionPixelSize(R.dimen.avatar_small));
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final ItemCommentBinding b;

            Holder(ItemCommentBinding binding) {
                super(binding.getRoot());
                b = binding;
            }
        }
    }
}
