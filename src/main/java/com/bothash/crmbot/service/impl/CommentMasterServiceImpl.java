package com.bothash.crmbot.service.impl;

import com.bothash.crmbot.entity.CommentMaster;
import com.bothash.crmbot.repository.CommentMasterRepository;
import com.bothash.crmbot.service.CommentMasterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentMasterServiceImpl implements CommentMasterService {

    @Autowired
    private CommentMasterRepository commentMasterRepository;

    @Override
    public List<CommentMaster> getAllComments() {
        return commentMasterRepository.findAll();
    }

    @Override
    public CommentMaster getCommentById(Long id) {
        return commentMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + id));
    }

    @Override
    public CommentMaster createComment(CommentMaster commentMaster) {
        return commentMasterRepository.save(commentMaster);
    }

    @Override
    public CommentMaster updateComment(Long id, CommentMaster updatedComment) {
        CommentMaster existing = getCommentById(id);
        existing.setCommentText(updatedComment.getCommentText());
        return commentMasterRepository.save(existing);
    }

    @Override
    public void deleteComment(Long id) {
        commentMasterRepository.deleteById(id);
    }
}
