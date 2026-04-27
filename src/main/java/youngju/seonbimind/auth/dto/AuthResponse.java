package youngju.seonbimind.auth.dto;

public record AuthResponse(
        String name,
        String accessToken,
        String loginId
) {
}
