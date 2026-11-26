package com.gayatripatel.photofeed.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gayatripatel.photofeed.core.FeedPager;
import com.gayatripatel.photofeed.core.Post;
import com.gayatripatel.photofeed.core.SampleFeed;
import com.gayatripatel.photofeed.data.FeedRepository;
import com.gayatripatel.photofeed.databinding.FragmentFeedBinding;

public class FeedFragment extends Fragment {
    private FragmentFeedBinding b;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentFeedBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FeedViewModel vm = new ViewModelProvider(this).get(FeedViewModel.class);
        FeedRepository repository = FeedRepository.get();
        FeedAdapter adapter = new FeedAdapter(repository, SampleFeed.ZONE, new FeedAdapter.Listener() {
            @Override
            public void onLike(Post post, boolean liked) {
                vm.setLiked(post, liked);
            }

            @Override
            public void onSave(Post post) {
                vm.toggleSaved(post);
            }

            @Override
            public void onComments(Post post) {
                startActivity(CommentsActivity.intent(requireContext(), post.id));
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        b.feed.setLayoutManager(layoutManager);
        b.feed.setAdapter(adapter);
        b.feed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 0 && FeedPager.shouldPrefetch(layoutManager.findLastVisibleItemPosition(), adapter.getItemCount(), 2)) {
                    vm.loadMore();
                }
            }
        });
        b.swipeRefresh.setOnRefreshListener(() -> {
            vm.refresh();
            b.swipeRefresh.setRefreshing(false);
        });
        vm.state().observe(getViewLifecycleOwner(), s -> adapter.submit(s.posts, s.loading, s.endReached));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
