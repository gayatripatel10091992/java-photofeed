package com.gayatripatel.photofeed.core;

public final class Comment {
    public final String id;
    public final String postId;
    public final String authorId;
    public final String text;
    public final long createdAtMillis;

    public Comment(String id, String postId, String authorId, String text, long createdAtMillis) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.text = text;
        this.createdAtMillis = createdAtMillis;
    }
}
