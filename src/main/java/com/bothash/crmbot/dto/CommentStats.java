package com.bothash.crmbot.dto;

import com.bothash.crmbot.entity.Comments;

public class CommentStats {
    private final int count;
    private final Comments mostRecent;

    public CommentStats(int count, Comments mostRecent) {
        this.count = count;
        this.mostRecent = mostRecent;
    }

    public int getCount() {
        return count;
    }

    public Comments getMostRecent() {
        return mostRecent;
    }
}

