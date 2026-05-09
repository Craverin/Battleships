package gamestudio.server.service.jpa;

import gamestudio.server.entity.Comment;
import gamestudio.server.service.CommentService;
import gamestudio.server.service.exception.CommentException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
public class CommentServiceJPA implements CommentService
{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addComment(Comment comment) throws CommentException
    {
        entityManager.persist(comment);
    }

    @Override
    public List<Comment> getComments(String game) throws CommentException
    {
        return entityManager.createNamedQuery("Comment.getComments", Comment.class)
                .setParameter("game", game).getResultList();
    }

    @Override
    public List<Comment> getPlayerComments(String game, String player)
    {
        return entityManager.createNamedQuery("Comment.getPlayerComments", Comment.class)
            .setParameter("game", game).setParameter("player", player).getResultList();
    }

    @Override
    public void reset() throws CommentException
    {
        entityManager.createNativeQuery("DELETE FROM Comment").executeUpdate();
    }
}
