package com.gayatripatel.photofeed.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gayatripatel.photofeed.R;
import com.gayatripatel.photofeed.core.Formats;
import com.gayatripatel.photofeed.core.Post;
import com.gayatripatel.photofeed.core.User;
import com.gayatripatel.photofeed.data.FeedRepository;
import com.gayatripatel.photofeed.databinding.FragmentProfileBinding;
import com.gayatripatel.photofeed.databinding.ItemGridPhotoBinding;
import com.gayatripatel.photofeed.databinding.ItemProfileHeaderBinding;
import com.gayatripatel.photofeed.image.PhotoLoader;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding b;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentProfileBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FeedRepository repository = FeedRepository.get();
        User me = repository.me();
        b.toolbar.setTitle(me.username);

        ProfileAdapter adapter = new ProfileAdapter(me);
        GridLayoutManager grid = new GridLayoutManager(requireContext(), 3);
        grid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? 3 : 1;
            }
        });
        b.grid.setLayoutManager(grid);
        b.grid.setAdapter(adapter);
        repository.posts().observe(getViewLifecycleOwner(), all -> adapter.submit(repository.postsBy(me.id)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }

    private static final class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final User me;
        private final List<Post> posts = new ArrayList<>();

        ProfileAdapter(User me) {
            this.me = me;
        }

        @android.annotation.SuppressLint("NotifyDataSetChanged")
        void submit(List<Post> mine) {
            posts.clear();
            posts.addAll(mine);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return posts.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? 0 : 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == 0) return new HeaderHolder(ItemProfileHeaderBinding.inflate(inflater, parent, false));
            return new TileHolder(ItemGridPhotoBinding.inflate(inflater, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HeaderHolder) {
                ItemProfileHeaderBinding h = ((HeaderHolder) holder).b;
                h.avatarView.ring.setBackgroundResource(R.drawable.bg_story_ring);
                PhotoLoader.loadAvatar(h.avatarView.avatar, me, holder.itemView.getResources().getDimensionPixelSize(R.dimen.avatar_story));
                h.postsCount.setText(stat(Formats.count(posts.size()), holder.itemView.getContext().getString(R.string.posts)));
                h.followersCount.setText(stat(Formats.count(me.followers), holder.itemView.getContext().getString(R.string.followers)));
                h.followingCount.setText(stat(Formats.count(me.following), holder.itemView.getContext().getString(R.string.following)));
                h.displayName.setText(me.displayName);
                h.bio.setText(me.bio);
            } else {
                TileHolder t = (TileHolder) holder;
                Post p = posts.get(position - 1);
                int size = holder.itemView.getResources().getDisplayMetrics().widthPixels / 3;
                // Grid tiles are square crops; decode them at tile size, not full width.
                PhotoLoader.loadPost(t.b.tile, new Post(p.id, p.authorId, p.photoSeed, p.localPath, 1f, p.caption, p.location,
                        p.createdAtMillis, p.likeCount, p.commentCount, p.likedByMe, p.savedByMe), size);
            }
        }

        private static CharSequence stat(String value, String label) {
            android.text.SpannableStringBuilder sb = new android.text.SpannableStringBuilder();
            sb.append(value, new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.append('\n').append(label);
            return sb;
        }

        private static final class HeaderHolder extends RecyclerView.ViewHolder {
            final ItemProfileHeaderBinding b;

            HeaderHolder(ItemProfileHeaderBinding binding) {
                super(binding.getRoot());
                b = binding;
            }
        }

        private static final class TileHolder extends RecyclerView.ViewHolder {
            final ItemGridPhotoBinding b;

            TileHolder(ItemGridPhotoBinding binding) {
                super(binding.getRoot());
                b = binding;
            }
        }
    }
}
