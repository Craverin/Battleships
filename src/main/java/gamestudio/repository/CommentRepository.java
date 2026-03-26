package gamestudio.repository;

import gamestudio.entity.Comment;
import gamestudio.repository.exception.CommentException;

import java.util.List;

public interface CommentRepository
{
    void addComment(Comment comment) throws CommentException;
    List<Comment> getComments(String game) throws CommentException;
    List<Comment> getPlayerComments(String game, String player);
    void reset() throws CommentException;
}
