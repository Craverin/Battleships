package gamestudio.server.service;

import gamestudio.server.dto.PlayerStatResponse;
import gamestudio.server.entity.PlayerStats;

import java.util.List;

public interface PlayerStatsService
{
    void addPlayerStats(String game, String player, int score, boolean isWinner);
    PlayerStatResponse getPlayerStats(String game, String player);
    List<PlayerStats> getTopPlayers(String game, String sortBy, String sortType);
}
