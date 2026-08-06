package gamestudio.server.service;

import gamestudio.server.dto.AddCommentRequest;
import gamestudio.server.entity.Comment;
import gamestudio.server.entity.User;
import gamestudio.server.service.authentication.CurrentUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CommentService
{
    @PersistenceContext
    private EntityManager entityManager;

    private final CurrentUserService currentUserService;

    public CommentService(CurrentUserService currentUserService)
    {
        this.currentUserService = currentUserService;
    }

    public void addComment(String game, String comment)
    {
        int userId = currentUserService.getCurrentUserId();
        User user = entityManager.getReference(User.class, userId);

        entityManager.persist(new Comment(user, game, comment, new Date()));
    }

    public List<Comment> getComments(String game)
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

    public void reset()
    {
        entityManager.createQuery("DELETE FROM Comment").executeUpdate();
    }
}
