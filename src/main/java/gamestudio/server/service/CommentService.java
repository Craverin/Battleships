package gamestudio.server.service;

import gamestudio.server.entity.Comment;
import gamestudio.server.service.exception.CommentException;

import java.util.List;

public interface CommentService
{
    void addComment(Comment comment) throws CommentException;
    List<Comment> getComments(String game) throws CommentException;
    List<Comment> getPlayerComments(String game, String player) throws CommentException;
    void reset() throws CommentException;
}
