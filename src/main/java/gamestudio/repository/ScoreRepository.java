package gamestudio.repository;

import gamestudio.entity.Score;
import gamestudio.repository.exception.ScoreException;

import java.util.List;

public interface ScoreRepository
{
    void addScore(Score score) throws ScoreException;
    List<Score> getTopScores(String game) throws ScoreException;
    int getTopScore(String game, String player) throws ScoreException;
    void reset() throws ScoreException;
}
