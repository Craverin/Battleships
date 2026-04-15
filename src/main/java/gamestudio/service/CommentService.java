package gamestudio.service;

import gamestudio.entity.Comment;
import gamestudio.service.exception.CommentException;

import java.util.List;

public interface CommentService
{
    void addComment(Comment comment) throws CommentException;
    List<Comment> getComments(String game) throws CommentException;
    List<Comment> getPlayerComments(String game, String player) throws CommentException;
    void reset() throws CommentException;
}
