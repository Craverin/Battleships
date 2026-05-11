package gamestudio.server.service;

import gamestudio.server.dto.AddCommentRequest;
import gamestudio.server.entity.Comment;
import gamestudio.server.service.exception.CommentException;

import java.util.List;

public interface CommentService
{
    void addComment(String game, AddCommentRequest comment) throws CommentException;
    List<Comment> getComments(String game) throws CommentException;
    List<Comment> getMyComments(String game) throws CommentException;
    void reset() throws CommentException;
}
