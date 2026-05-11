package gamestudio.server.service.jpa;

import gamestudio.server.dto.AddCommentRequest;
import gamestudio.server.entity.Comment;
import gamestudio.server.entity.User;
import gamestudio.server.service.CommentService;
import gamestudio.server.service.authentication.CurrentUserService;
import gamestudio.server.service.exception.CommentException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Transactional
@Service
public class CommentServiceJPA implements CommentService
{
    @PersistenceContext
    private EntityManager entityManager;

    private final CurrentUserService currentUserService;

    public CommentServiceJPA(CurrentUserService currentUserService)
    {
        this.currentUserService = currentUserService;
    }

    @Override
    public void addComment(String game, AddCommentRequest comment) throws CommentException
    {
        int userId = currentUserService.getCurrentUserId();
        User user = entityManager.getReference(User.class, userId);

        entityManager.persist(new Comment(user, game, comment.comment(), new Date()));
    }

    @Override
    public List<Comment> getComments(String game) throws CommentException
    {
        return entityManager.createNamedQuery("Comment.getComments", Comment.class)
                .setParameter("game", game).getResultList();
    }

    public List<Comment> getMyComments(String game)
    {
        int userId = currentUserService.getCurrentUserId();

        return entityManager.createNamedQuery("Comment.getPlayerComments", Comment.class)
            .setParameter("game", game).setParameter("userId", userId).getResultList();
    }

    @Override
    public void reset() throws CommentException
    {
        entityManager.createNativeQuery("DELETE FROM Comment").executeUpdate();
    }
}
