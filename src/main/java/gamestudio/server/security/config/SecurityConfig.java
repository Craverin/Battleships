package gamestudio.server.security.config;

import gamestudio.server.security.oauth.ApplicationOAuth2UserService;
import gamestudio.server.security.oauth.ApplicationOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig
{
    private final ApplicationOidcUserService applicationOidcUserService;
    private final ApplicationOAuth2UserService applicationOAuthUserService;

    public SecurityConfig(ApplicationOidcUserService applicationOidcUserService,
                          ApplicationOAuth2UserService applicationOAuthUserService)
    {
        this.applicationOidcUserService = applicationOidcUserService;
        this.applicationOAuthUserService = applicationOAuthUserService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/me").permitAll()

                        .requestMatchers(
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/comments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/rating/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/stats/**").permitAll()

                        .requestMatchers("/games/**").permitAll()
                        .requestMatchers("/events/**").permitAll()

                        .requestMatchers("/error").permitAll()

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(applicationOidcUserService)
                                .userService(applicationOAuthUserService)
                        )
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
