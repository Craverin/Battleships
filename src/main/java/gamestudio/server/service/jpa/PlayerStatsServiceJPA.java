package gamestudio.server.service.jpa;

import gamestudio.server.dto.PlayerStatResponse;
import gamestudio.server.entity.PlayerStats;
import gamestudio.server.service.PlayerStatsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
public class PlayerStatsServiceJPA implements PlayerStatsService
{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addPlayerStats(String game, String player, int score, boolean isWinner)
    {
        List<PlayerStats> stats = entityManager.createNamedQuery("PlayerStats.getPlayerStats", PlayerStats.class)
                .setParameter("game", game).setParameter("player", player).getResultList();

        int gamesWonIncrement = isWinner ? 1 : 0;
        if (stats.isEmpty())
        {
            entityManager.persist(new PlayerStats(player, game, 1, gamesWonIncrement, score, score));
        }

        else
        {
            PlayerStats stat = stats.get(0);
            stat.setGamesPlayed(stat.getGamesPlayed() + 1);
            stat.setGamesWon(stat.getGamesWon() + gamesWonIncrement);
            stat.setTotalScore(stat.getTotalScore() + score);
            stat.setBestScore(Math.max(stat.getBestScore(), score));
        }
    }

    @Override
    public PlayerStatResponse getPlayerStats(String game, String player)
    {
        List<PlayerStats> stats = entityManager.createNamedQuery("PlayerStats.getPlayerStats", PlayerStats.class)
                .setParameter("game", game).setParameter("player", player).getResultList();

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

    @Override
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
