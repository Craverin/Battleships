package gamestudio.server.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword) { }
