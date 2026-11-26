package com.gayatripatel.photofeed.core;

/** Immutable post. Like/save changes produce a copy, which keeps RecyclerView diffing simple. */
public final class Post {
    public final String id;
    public final String authorId;
    /** Seed for generated demo photos; ignored when {@link #localPath} points to a picked photo. */
    public final int photoSeed;
    public final String localPath;
    /** Width / height. Feed images are clamped between 4:5 and 1.91:1 like other photo apps. */
    public final float aspectRatio;
    public final String caption;
    public final String location;
    public final long createdAtMillis;
    public final int likeCount;
    public final int commentCount;
    public final boolean likedByMe;
    public final boolean savedByMe;

    public Post(String id, String authorId, int photoSeed, String localPath, float aspectRatio, String caption, String location,
                long createdAtMillis, int likeCount, int commentCount, boolean likedByMe, boolean savedByMe) {
        this.id = id;
        this.authorId = authorId;
        this.photoSeed = photoSeed;
        this.localPath = localPath;
        this.aspectRatio = clampAspect(aspectRatio);
        this.caption = caption;
        this.location = location;
        this.createdAtMillis = createdAtMillis;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.likedByMe = likedByMe;
        this.savedByMe = savedByMe;
    }

    public static float clampAspect(float ratio) {
        return Math.max(0.8f, Math.min(1.91f, ratio));
    }

    public Post withLike(boolean liked) {
        if (liked == likedByMe) return this;
        return new Post(id, authorId, photoSeed, localPath, aspectRatio, caption, location, createdAtMillis,
                Math.max(0, likeCount + (liked ? 1 : -1)), commentCount, liked, savedByMe);
    }

    public Post withSaved(boolean saved) {
        return new Post(id, authorId, photoSeed, localPath, aspectRatio, caption, location, createdAtMillis,
                likeCount, commentCount, likedByMe, saved);
    }

    public Post withCommentAdded() {
        return new Post(id, authorId, photoSeed, localPath, aspectRatio, caption, location, createdAtMillis,
                likeCount, commentCount + 1, likedByMe, savedByMe);
    }
}
