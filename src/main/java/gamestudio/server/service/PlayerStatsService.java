package gamestudio.server.service;

import gamestudio.server.dto.PlayerStatResponse;
import gamestudio.server.entity.PlayerStats;

import java.util.List;

public interface PlayerStatsService
{
    void recordGameResult(String game, int userId, String username, int score, boolean isWinner);
    PlayerStatResponse getMyStats(String game);
    List<PlayerStats> getTopPlayers(String game, String sortBy, String sortType);
}
