package gamestudio.jdbc;

import gamestudio.entity.Rating;
import gamestudio.service.jdbc.JdbcRatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest(properties = {
        "spring.sql.init.schema-locations=classpath:jdbc-schema.sql",
        "spring.sql.init.mode=always"
})
@Testcontainers
@ContextConfiguration(classes = JdbcRatingServiceTest.TestConfig.class)
@Import(JdbcRatingService.class)
public class JdbcRatingServiceTest
{
    @Autowired
    private JdbcRatingService repository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @SpringBootConfiguration
    static class TestConfig { }

    @Test
    public void getRating_threeRatingsAdded_returnsCorrectRatings()
    {
        repository.setRating(new Rating("p1", "battleships", 5, new Date()));
        repository.setRating(new Rating("p2", "numberLink", 2, new Date()));
        repository.setRating(new Rating("p3", "sudoku", 3, new Date()));

        assertEquals(5, repository.getRating("battleships", "p1" ));
        assertEquals(2, repository.getRating("numberLink", "p2"));
        assertEquals(3, repository.getRating("sudoku", "p3"));
    }

    @Test
    public void getRating_noRatings_returnsMinusOne()
    {
        assertEquals(-1, repository.getRating("p1", "battleships"));
    }

    @Test
    public void setRating_noRating_savesRating()
    {
        repository.setRating(new Rating("p1", "battleships", 4, new Date()));

        int rating = repository.getRating("battleships", "p1");
        assertEquals(4, rating);
    }

    @Test
    public void setRating_ratingAlreadyExists_updatesRating()
    {
        repository.setRating(new Rating("p1", "battleships", 4, new Date()));
        repository.setRating(new Rating("p1", "battleships", 2, new Date()));
        repository.setRating(new Rating("p1", "battleships", 5, new Date()));

        assertEquals(5, repository.getRating("battleships", "p1"));
    }

    @Test
    public void getAverageRating_noRatings_returnsMinusOne()
    {
        assertEquals(-1, repository.getAverageRating("battleships"));
    }

    @Test
    public void getAverageRating_ratingsAlreadyExist_returnsAverageRating()
    {
        int r1 = 1, r2 = 3, r3 = 4, r4 = 2, r5 = 5, r6 = 5;
        int average = (r1 + r2 + r3 + r4 + r5 + r6) / 6;

        repository.setRating(new Rating("p1", "battleships", r1, new Date()));
        repository.setRating(new Rating("p2", "battleships", r2, new Date()));
        repository.setRating(new Rating("p3", "battleships", r3, new Date()));
        repository.setRating(new Rating("p4", "battleships", r4, new Date()));
        repository.setRating(new Rating("p5", "battleships", r5, new Date()));
        repository.setRating(new Rating("p6", "battleships", r6, new Date()));

        assertEquals(average, repository.getAverageRating("battleships"));
    }

    @Test
    public void reset_afterAddingRatings_deletesAllRatings()
    {
        repository.setRating(new Rating("p1", "battleships", 4, new Date()));
        assertEquals(4, repository.getRating("battleships", "p1"));

        repository.reset();
        assertEquals(-1, repository.getRating("battleships", "p1"));
    }
}
