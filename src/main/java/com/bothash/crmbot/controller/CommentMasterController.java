package com.bothash.crmbot.controller;

import com.bothash.crmbot.entity.CommentMaster;
import com.bothash.crmbot.service.CommentMasterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commentmaster/api")
@CrossOrigin(origins = "*") // Optional: allows access from frontend apps
public class CommentMasterController {

    @Autowired
    private CommentMasterService commentMasterService;

    // GET all comments
    @GetMapping
    public ResponseEntity<List<CommentMaster>> getAllComments() {
        return ResponseEntity.ok(commentMasterService.getAllComments());
    }

    // GET a comment by ID
    @GetMapping("/{id}")
    public ResponseEntity<CommentMaster> getCommentById(@PathVariable Long id) {
        return ResponseEntity.ok(commentMasterService.getCommentById(id));
    }

    // POST - Create a new comment
    @PostMapping("/create")
    public ResponseEntity<CommentMaster> createComment(@RequestBody CommentMaster commentMaster) {
        return ResponseEntity.ok(commentMasterService.createComment(commentMaster));
    }

    // PUT - Update an existing comment
    @PutMapping("/{id}")
    public ResponseEntity<CommentMaster> updateComment(
            @PathVariable Long id,
            @RequestBody CommentMaster commentMaster
    ) {
        return ResponseEntity.ok(commentMasterService.updateComment(id, commentMaster));
    }

    // DELETE - Delete a comment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentMasterService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
