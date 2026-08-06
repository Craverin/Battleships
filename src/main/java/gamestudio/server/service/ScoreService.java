package gamestudio.server.service;

import gamestudio.server.entity.Score;
import gamestudio.server.entity.User;
import gamestudio.server.service.authentication.CurrentUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Transactional
@Service
public class ScoreService
{
    @PersistenceContext
    private EntityManager entityManager;

    private final CurrentUserService currentUserService;

    public ScoreService(CurrentUserService currentUserService)
    {
        this.currentUserService = currentUserService;
    }

    public void addScore(String game, int userId, String username, int score)
    {
        User user = entityManager.getReference(User.class, userId);

        entityManager.persist(new Score(user, game, score, new Date()));
    }

    public List<Score> getTopScores(String game)
    {
        return entityManager.createNamedQuery("Score.getTopScores", Score.class)
                .setParameter("game", game).setMaxResults(10).getResultList();
    }

    public int getMyTopScore(String game)
    {
        int userId = currentUserService.getCurrentUserId();
        Integer topScore = entityManager.createNamedQuery("Score.getTopScore", Integer.class)
                .setParameter("game", game).setParameter("userId", userId)
                .setMaxResults(10).getSingleResultOrNull();

        return topScore == null ? -1 : topScore;
    }

    public void reset()
    {
        entityManager.createNativeQuery("DELETE FROM Score").executeUpdate();
    }
}
