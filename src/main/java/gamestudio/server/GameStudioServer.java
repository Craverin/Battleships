package gamestudio.server;

import gamestudio.server.service.CommentService;
import gamestudio.server.service.RatingService;
import gamestudio.server.service.ScoreService;
import gamestudio.server.service.jpa.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EntityScan("gamestudio.server.entity")
@EnableScheduling
public class GameStudioServer
{
    public static void main(String[] args)
    {
        SpringApplication.run(GameStudioServer.class, args);
    }

}
