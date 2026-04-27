package youngju.seonbimind.auth.dto;

public record AuthResult(
        AuthResponse response,
        String refreshToken
) {
}
