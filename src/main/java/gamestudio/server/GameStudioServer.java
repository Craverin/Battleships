package gamestudio.server;

import gamestudio.service.CommentService;
import gamestudio.service.RatingService;
import gamestudio.service.ScoreService;
import gamestudio.service.jpa.CommentServiceJPA;
import gamestudio.service.jpa.RatingServiceJPA;
import gamestudio.service.jpa.ScoreServiceJPA;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EntityScan("gamestudio.entity")
public class GameStudioServer
{
    public static void main(String[] args)
    {
        SpringApplication.run(GameStudioServer.class, args);
    }

    @Bean
    public ScoreService scoreService()
    {
        return new ScoreServiceJPA();
    }

    @Bean
    public RatingService ratingService()
    {
        return new RatingServiceJPA();
    }

    @Bean
    public CommentService commentService()
    {
        return new CommentServiceJPA();
    }

    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }
}
