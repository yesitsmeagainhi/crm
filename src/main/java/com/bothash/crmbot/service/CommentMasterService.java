package com.bothash.crmbot.service;

import com.bothash.crmbot.entity.CommentMaster;
import java.util.List;

public interface CommentMasterService {

    List<CommentMaster> getAllComments();

    CommentMaster getCommentById(Long id);

    CommentMaster createComment(CommentMaster commentMaster);

    CommentMaster updateComment(Long id, CommentMaster commentMaster);

    void deleteComment(Long id);
}
