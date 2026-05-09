package gamestudio.server.service;

import gamestudio.server.entity.Score;
import gamestudio.server.service.exception.ScoreException;

import java.util.List;

public interface ScoreService
{
    void addScore(Score score) throws ScoreException;
    List<Score> getTopScores(String game) throws ScoreException;
    int getTopScore(String game, String player) throws ScoreException;
    void reset() throws ScoreException;
}
