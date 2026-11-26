package com.gayatripatel.photofeed.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gayatripatel.photofeed.R;
import com.gayatripatel.photofeed.core.SampleFeed;
import com.gayatripatel.photofeed.core.User;
import com.gayatripatel.photofeed.data.FeedRepository;
import com.gayatripatel.photofeed.databinding.ItemStoryBinding;
import com.gayatripatel.photofeed.image.PhotoLoader;
import java.util.List;

final class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.Holder> {
    private final FeedRepository repository;
    private final List<String> order = SampleFeed.storyOrder();

    StoriesAdapter(FeedRepository repository) {
        this.repository = repository;
    }

    @Override
    public int getItemCount() {
        return order.size();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemStoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        User u = repository.user(order.get(position));
        boolean me = SampleFeed.ME.equals(u.id);
        h.b.name.setText(me ? h.itemView.getContext().getString(R.string.your_story) : u.username);
        h.b.avatarView.ring.setBackgroundResource(SampleFeed.storySeen(u.id) ? R.drawable.bg_story_seen : R.drawable.bg_story_ring);
        PhotoLoader.loadAvatar(h.b.avatarView.avatar, u, h.itemView.getResources().getDimensionPixelSize(R.dimen.avatar_story));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemStoryBinding b;

        Holder(ItemStoryBinding binding) {
            super(binding.getRoot());
            b = binding;
        }
    }
}
