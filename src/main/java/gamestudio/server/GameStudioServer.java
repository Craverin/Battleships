package gamestudio.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

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
