package gamestudio;

import gamestudio.service.CommentService;
import gamestudio.service.RatingService;
import gamestudio.service.ScoreService;
import gamestudio.service.rest.CommentServiceRestClient;
import gamestudio.service.rest.RatingServiceRestClient;
import gamestudio.service.rest.ScoreServiceRestClient;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
               pattern = "gamestudio.server.*"))
public class SpringClient
{
	public static void main(String[] args)
    {
		new SpringApplicationBuilder(SpringClient.class).web(WebApplicationType.NONE).run(args);
	}

    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }

    @Bean
    public ScoreService scoreService(RestTemplate restTemplate) { return new ScoreServiceRestClient(restTemplate); }

    @Bean
    public RatingService ratingService(RestTemplate restTemplate)
    {
        return new RatingServiceRestClient(restTemplate);
    }

    @Bean
    public CommentService commentService(RestTemplate restTemplate)
    {
        return new CommentServiceRestClient(restTemplate);
    }
}
