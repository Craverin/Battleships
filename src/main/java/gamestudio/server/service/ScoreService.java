package gamestudio.server.service;

import gamestudio.server.entity.Score;
import gamestudio.server.service.exception.ScoreException;

import java.util.List;

public interface ScoreService
{
    void addScore(String game, int userId, String username, int score) throws ScoreException;
    List<Score> getTopScores(String game) throws ScoreException;
    int getMyTopScore(String game) throws ScoreException;
    void reset() throws ScoreException;
}
