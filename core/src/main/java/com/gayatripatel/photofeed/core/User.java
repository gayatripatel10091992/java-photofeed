package com.gayatripatel.photofeed.core;

public final class User {
    public final String id;
    public final String username;
    public final String displayName;
    public final String bio;
    public final int followers;
    public final int following;
    public final boolean verified;
    /** Seed for the generated avatar art. */
    public final int avatarSeed;

    public User(String id, String username, String displayName, String bio, int followers, int following, boolean verified, int avatarSeed) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.followers = followers;
        this.following = following;
        this.verified = verified;
        this.avatarSeed = avatarSeed;
    }
}
