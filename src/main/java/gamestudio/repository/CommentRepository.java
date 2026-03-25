package gamestudio.repository;

import gamestudio.entity.Comment;
import gamestudio.repository.exception.CommentException;

import java.util.List;

public interface CommentRepository
{
    void addComment(Comment comment) throws CommentException;
    List<Comment> getComments(String game) throws CommentException;
    Comment getComment(String game, String player);
    void reset() throws CommentException;
}
