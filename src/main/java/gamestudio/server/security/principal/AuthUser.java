package gamestudio.server.security.principal;

public record AuthUser(int userId, String username) implements ApplicationPrincipal { }
