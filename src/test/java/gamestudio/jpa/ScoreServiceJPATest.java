package gamestudio.jpa;

import gamestudio.entity.Score;
import gamestudio.service.jpa.RatingServiceJPA;
import gamestudio.service.jpa.ScoreServiceJPA;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@ContextConfiguration(classes = ScoreServiceJPATest.TestConfig.class)
@EntityScan("gamestudio.entity")
@Import(ScoreServiceJPA.class)
public class ScoreServiceJPATest
{
    @Autowired
    private ScoreServiceJPA repository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfig { }

    @Test
    public void getTopScore_afterAddingMultipleScores_returnsHighestScore()
    {
        repository.addScore(new Score("p1", "battleships", 150, new Date()));
        repository.addScore(new Score("p1", "battleships", 210, new Date()));
        repository.addScore(new Score("p1", "battleships", 900, new Date()));
        repository.addScore(new Score("p1", "battleships", 760, new Date()));
        repository.addScore(new Score("p1", "battleships", 890, new Date()));

        assertEquals(900, repository.getTopScore("battleships", "p1"));
    }

    @Test
    public void getTopScore_noScores_returnsMinusOne()
    {
        assertEquals(-1, repository.getTopScore("battleships", "p1"));
    }

    @Test
    public void getTopScores_afterAddingMultipleScores_returnsTenHighestScores()
    {
        int highestScore = 777, lowestScore = 300;
        Random rnd = new Random();

        List<Score> scores = new ArrayList<>()
        {
            {
                add(new Score("p1", "battleships", highestScore, new Date()));
                add(new Score("p1", "battleships", 700, new Date()));
                add(new Score("p1", "battleships", 666, new Date()));
                add(new Score("p1", "battleships", 600, new Date()));
                add(new Score("p1", "battleships", 555, new Date()));
                add(new Score("p1", "battleships", 500, new Date()));
                add(new Score("p1", "battleships", 444, new Date()));
                add(new Score("p1", "battleships", 400, new Date()));
                add(new Score("p1", "battleships", 333, new Date()));
                add(new Score("p1", "battleships", lowestScore, new Date()));
            }
        };

        for (Score score : scores)
            repository.addScore(score);

        for (int i = 0; i < 15; i++)
            repository.addScore(new Score("p1", "battleships", rnd.nextInt(100, lowestScore), new Date()));

        List<Score> topScores = repository.getTopScores("battleships");
        for (int i = 0; i < topScores.size(); i++)
        {
            Score expectedScore = scores.get(i);
            Score score = topScores.get(i);

            assertEquals(expectedScore.getPlayer(), score.getPlayer());
            assertEquals(expectedScore.getGame(), score.getGame());
            assertEquals(expectedScore.getPoints(), score.getPoints());
        }
    }

    @Test
    public void getTopScores_noScores_returnsEmptyList()
    {
        assertTrue(repository.getTopScores("battleships").isEmpty());
    }

    @Test
    public void addScore_validScore_savesScore()
    {
        repository.addScore(new Score("p1", "battleships", 200, new Date()));

        int score = repository.getTopScore("battleships", "p1");
        assertEquals(200, score);
    }

    @Test
    public void reset_afterAddingScores_deletesAllScores()
    {
        repository.addScore(new Score("p1", "battleships", 200, new Date()));
        repository.addScore(new Score("p1", "battleships", 250, new Date()));
        repository.addScore(new Score("p1", "battleships", 300, new Date()));

        assertFalse(repository.getTopScores("battleships").isEmpty());
        repository.reset();
        assertTrue(repository.getTopScores("battleships").isEmpty());
    }
}
