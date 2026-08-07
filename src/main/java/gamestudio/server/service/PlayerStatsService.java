package gamestudio.server.service;

import gamestudio.server.dto.PlayerStatResponse;
import gamestudio.server.entity.PlayerStats;
import gamestudio.server.entity.User;
import gamestudio.server.service.authentication.CurrentUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Service
public class PlayerStatsService
{
    @PersistenceContext
    private EntityManager entityManager;

    private final CurrentUserService currentUserService;

    public PlayerStatsService(CurrentUserService currentUserService)
    {
        this.currentUserService = currentUserService;
    }

    public void recordGameResult(String game, int userId, String username, int score, boolean isWinner)
    {
        User user = entityManager.getReference(User.class, userId);
        List<PlayerStats> stats = entityManager.createNamedQuery("PlayerStats.getPlayerStats", PlayerStats.class)
                .setParameter("game", game).setParameter("userId", userId).getResultList();

        int gamesWonIncrement = isWinner ? 1 : 0;
        if (stats.isEmpty())
            entityManager.persist(new PlayerStats(user, game, 1, gamesWonIncrement, score, score));

        else
        {
            PlayerStats stat = stats.get(0);
            stat.setGamesPlayed(stat.getGamesPlayed() + 1);
            stat.setGamesWon(stat.getGamesWon() + gamesWonIncrement);
            stat.setTotalScore(stat.getTotalScore() + score);
            stat.setBestScore(Math.max(stat.getBestScore(), score));
        }
    }

    public PlayerStatResponse getMyStats(String game)
    {
        int userId = currentUserService.getCurrentUserId();
        List<PlayerStats> stats = entityManager.createNamedQuery("PlayerStats.getPlayerStats", PlayerStats.class)
                .setParameter("game", game).setParameter("userId", userId).getResultList();

        if (stats.isEmpty()) return null;

        PlayerStats stat = stats.get(0);
        return new PlayerStatResponse(stat.getIdent(),
                stat.getGame(),
                stat.getPlayer(),
                stat.getGamesPlayed(),
                stat.getGamesWon(),
                stat.getTotalScore(),
                stat.getBestScore(),
                getRank(game, stat.getBestScore(), stat.getTotalScore()));
    }

    public List<PlayerStats> getTopPlayers(String game, String sortBy, String sortType)
    {
        String orderType = "ASC".equalsIgnoreCase(sortType) ? "ASC" : "DESC";
        String orderBy = getOrderBy(sortBy);

        String query = "SELECT s FROM PlayerStats s WHERE s.game = :game ORDER BY " + orderBy + " " + orderType;

        return entityManager.createQuery(query, PlayerStats.class)
                .setParameter("game", game).setMaxResults(100).getResultList();
    }

    private int getRank(String game, int bestScore, int totalScore)
    {
        Long place = entityManager.createNamedQuery("PlayerStats.getRank", Long.class)
                .setParameter("game", game)
                .setParameter("bestScore", bestScore)
                .setParameter("totalScore", totalScore)
                .getSingleResult();

        return place.intValue() + 1;
    }

    private String getOrderBy(String orderBy)
    {
        return switch (orderBy)
        {
            case "gamesPlayed" -> "s.gamesPlayed";
            case "gamesWon" -> "s.gamesWon";
            case "winRatio"  -> "CASE WHEN s.gamesPlayed = 0 THEN 0 ELSE (1.0 * s.gamesWon / s.gamesPlayed) END";
            case "totalScore" -> "s.totalScore";
            default -> "s.bestScore";
        };
    }
}
